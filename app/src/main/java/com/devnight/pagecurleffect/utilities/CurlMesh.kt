package com.devnight.pagecurleffect.utilities

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class CurlMesh(private val rows: Int, private val cols: Int) {
    private val vertexBuffer: FloatBuffer
    private val texBuffer: FloatBuffer
    private val baseVertices = mutableListOf<Float>()
    private val texCoords = mutableListOf<Float>()

    init {
        generateMesh()
        vertexBuffer = ByteBuffer.allocateDirect(baseVertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        texBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()

        texBuffer.put(texCoords.toFloatArray()).position(0)
    }

    private fun generateMesh() {
        for (i in 0 until rows - 1) {
            for (j in 0 until cols - 1) {
                val x0 = j.toFloat() / (cols - 1)
                val y0 = i.toFloat() / (rows - 1)
                val x1 = (j + 1).toFloat() / (cols - 1)
                val y1 = (i + 1).toFloat() / (rows - 1)

                addVertex(x0, y0)
                addVertex(x1, y0)
                addVertex(x0, y1)

                addVertex(x1, y0)
                addVertex(x1, y1)
                addVertex(x0, y1)
            }
        }
    }

    private fun addVertex(x: Float, y: Float) {
        baseVertices.add(x * 2 - 1)
        baseVertices.add(1 - y * 2)
        baseVertices.add(0f)
        texCoords.add(x)
        texCoords.add(y)
    }

    fun applyCurl(touchX: Float, touchY: Float, cornerX: Float) {
        val radius = 0.22f
        val cornerY = touchY.coerceIn(-1.0f, 1.0f)

        val dx = touchX - cornerX
        val dy = touchY - cornerY
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)

        val nx = dx / dist
        val ny = dy / dist

        val midX = (touchX + cornerX) / 2f
        val midY = (touchY + cornerY) / 2f

        for (i in 0 until baseVertices.size step 3) {
            val bx = baseVertices[i]
            val by = baseVertices[i+1]

            val d = (bx - midX) * nx + (by - midY) * ny

            if (d > 0) {
                val theta = d / radius
                if (theta < PI) {
                    val sinT = sin(theta)
                    val cosT = cos(theta)
                    vertexBuffer.put(i, bx - nx * (d - radius * sinT))
                    vertexBuffer.put(i + 1, by - ny * (d - radius * sinT))
                    vertexBuffer.put(i + 2, radius * (1 - cosT))
                } else {
                    val rx = bx - 2 * nx * d + nx * PI.toFloat() * radius
                    val ry = by - 2 * ny * d + ny * PI.toFloat() * radius
                    vertexBuffer.put(i, rx)
                    vertexBuffer.put(i + 1, ry)
                    vertexBuffer.put(i + 2, radius * 2.02f)
                }
            } else {
                vertexBuffer.put(i, bx)
                vertexBuffer.put(i + 1, by)
                vertexBuffer.put(i + 2, 0f)
            }
        }
        vertexBuffer.position(0)
    }

    fun draw(program: Int, texture: Int) {
        GLES20.glUseProgram(program)
        val pos = GLES20.glGetAttribLocation(program, "aPosition")
        val tex = GLES20.glGetAttribLocation(program, "aTexCoord")

        GLES20.glEnableVertexAttribArray(pos)
        GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(tex)
        GLES20.glVertexAttribPointer(tex, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, baseVertices.size / 3)
    }
}
