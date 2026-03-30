package com.devnight.pagecurleffect.utilities

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PageCurlRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var mesh: CurlMesh
    private lateinit var bgMesh: CurlMesh
    private var program: Int = 0
    private var frontTextureId = -1
    private var nextTextureId = -1
    private var touchX = 1.1f
    private var touchY = -1.1f
    private var currentCornerX = 1.0f
    private var pendingFront: Bitmap? = null
    private var pendingNext: Bitmap? = null

    private val mvpMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val vShader = """
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        varying float vZ;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord;
            vZ = aPosition.z;
        }
    """.trimIndent()

    private val fShader = """
        precision mediump float;
        varying vec2 vTexCoord;
        varying float vZ;
        uniform sampler2D uTexture;
        void main() {
            vec4 color = texture2D(uTexture, vTexCoord);
            float shadow = 1.0 - (vZ * 1.2); 
            gl_FragColor = vec4(color.rgb * shadow, color.a);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        mesh = CurlMesh(60, 60)
        bgMesh = CurlMesh(2, 2)

        val vs = loadShader(GLES20.GL_VERTEX_SHADER, vShader)
        val fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fShader)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 5.2f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES20.glViewport(0, 0, w, h)
        val ratio = w.toFloat() / h
        Matrix.perspectiveM(projMatrix, 0, 45f, ratio, 1f, 10f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        pendingFront?.let {
            frontTextureId = updateTex(it, frontTextureId)
            pendingFront = null
        }
        pendingNext?.let {
            nextTextureId = updateTex(it, nextTextureId)
            pendingNext = null
        }

        GLES20.glUseProgram(program)
        val uMVP = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvpMatrix, 0)

        if (nextTextureId != -1) {
            bgMesh.applyCurl(1.1f, -1.1f, 1.0f)
            bgMesh.draw(program, nextTextureId)
        }

        if (frontTextureId != -1) {
            mesh.applyCurl(touchX, touchY, currentCornerX)
            mesh.draw(program, frontTextureId)
        }
    }

    private fun updateTex(b: Bitmap, id: Int): Int {
        if (id != -1) GLES20.glDeleteTextures(1, intArrayOf(id), 0)
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0)
        return ids[0]
    }

    private fun loadShader(t: Int, c: String) = GLES20.glCreateShader(t).also {
        GLES20.glShaderSource(it, c)
        GLES20.glCompileShader(it)
    }

    fun setPages(f: Bitmap, n: Bitmap?) {
        pendingFront = f
        pendingNext = n
    }

    fun setTouch(x: Float, y: Float, cornerX: Float = 1.0f) {
        touchX = x
        touchY = y
        currentCornerX = cornerX
    }
}