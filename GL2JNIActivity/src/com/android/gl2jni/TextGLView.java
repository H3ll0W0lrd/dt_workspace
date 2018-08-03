package com.android.gl2jni;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;

public class TextGLView extends GLSurfaceView {

	private int mProgram;

	public TextGLView(Context context) {
		super(context);
		init();
	}

	public TextGLView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setRenderer(new Renderer());
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // 创建一个空的OpenGL ES Program
		GLES20.glAttachShader(mProgram, vertexShader); // 将vertex
														// shader添加到program
		GLES20.glAttachShader(mProgram, fragmentShader); // 将fragment
															// shader添加到program
		GLES20.glLinkProgram(mProgram); // 创建可执行的 OpenGL ES program
	}

	private final static String vertexShaderCode = "attribute vec4 vPosition;"
			+ "void main() {" + "  gl_Position = vPosition;" + "}";

	private final static String fragmentShaderCode = "precision mediump float;"
			+ "uniform vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	public static int loadShader(int type, String shaderCode) {

		// 创建一个vertex shader类型(GLES20.GL_VERTEX_SHADER)
		// 或fragment shader类型(GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// 将源码添加到shader并编译之
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}


	private class Renderer implements GLSurfaceView.Renderer {
		
		private final short[] VERTEX_INDEX = { 0, 1, 2, 0, 2, 3 };
		private final float[] VERTEX = { // in counterclockwise order:
			1, 1, 0, // top right
			-1, 1, 0, // top left
			-1, -1, 0, // bottom left
			1, -1, 0, // bottom right
		};
		
		public Renderer() {
		}

		public void onDrawFrame(GL10 gl) {
			// 将program加入OpenGL ES环境中
			GLES20.glUseProgram(mProgram);

			// 获取指向vertex shader的成员vPosition的 handle
			int mPositionHandle = GLES20.glGetAttribLocation(mProgram,
					"vPosition");

			// 启用一个指向三角形的顶点数组的handle
			GLES20.glEnableVertexAttribArray(mPositionHandle);

			// 准备三角形的坐标数据
			GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
					false, 0, 3);
			// 获取指向fragment shader的成员vColor的handle
			int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

			// 设置三角形的颜色
			GLES20.glUniform4fv(mColorHandle, 1, new float[]{110,34,68}, 0);

			// 画三角形
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

			// 禁用指向三角形的顶点数组
			GLES20.glDisableVertexAttribArray(mPositionHandle);
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// Do nothing.
		}
	}
}
