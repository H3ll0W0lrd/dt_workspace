package com.example.textdemo;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import dalvik.system.DexClassLoader;

public class JarActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jar_layout);
		TextView text = (TextView) findViewById(R.id.textView1);
		// test1();
		String libPath = Environment.getExternalStorageDirectory()
				+ File.separator + "dingtao.jar"; // 要动态加载的jar
		File dexDir = getDir("dex", MODE_PRIVATE); // 优化后dex的路径

		/**
		 * 进行动态加载，利用java的反射调用com.rtm.common.utils.RMFileUtil的方法
		 */
		DexClassLoader classLoader = new DexClassLoader(libPath,
				dexDir.getAbsolutePath(), null, getClassLoader());
		try {
			Class<?> cls = classLoader
					.loadClass("com.rtm.common.utils.RMFileUtil");
			Object object = cls.newInstance();
			Method method = cls.getMethod("getLibsDir");
			String str = (String) method.invoke(object);
			text.setText(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void test1() {
		String path = Environment.getExternalStorageDirectory()
				+ File.separator + "dingtao.jar";// 外部jar包的路径
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();// 所有的Class对象
		Map<Class<?>, Annotation[]> classAnnotationMap = new HashMap<Class<?>, Annotation[]>();// 每个Class对象上的注释对象
		Map<Class<?>, Map<Method, Annotation[]>> classMethodAnnoMap = new HashMap<Class<?>, Map<Method, Annotation[]>>();// 每个Class对象中每个方法上的注释对象
		try {
			@SuppressWarnings("resource")
			JarFile jarFile = new JarFile(new File(path));
			URL url = new URL("file:" + path);
			ClassLoader loader = new URLClassLoader(new URL[] { url });// 自己定义的classLoader类，把外部路径也加到load路径里，使系统去该路经load对象
			Enumeration<JarEntry> es = jarFile.entries();
			while (es.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry) es.nextElement();
				String name = jarEntry.getName();
				if (name != null && name.endsWith(".class")) {// 只解析了.class文件，没有解析里面的jar包
					// 默认去系统已经定义的路径查找对象，针对外部jar包不能用
					// Class<?> c =
					// Thread.currentThread().getContextClassLoader().loadClass(name.replace("/",
					// ".").substring(0,name.length() - 6));
					Class<?> c = loader.loadClass(name.replace("/", ".")
							.substring(0, name.length() - 6));// 自己定义的loader路径可以找到
					classes.add(c);
					Annotation[] classAnnos = c.getDeclaredAnnotations();
					classAnnotationMap.put(c, classAnnos);
					Method[] classMethods = c.getDeclaredMethods();
					Map<Method, Annotation[]> methodAnnoMap = new HashMap<Method, Annotation[]>();
					for (int i = 0; i < classMethods.length; i++) {
						Annotation[] a = classMethods[i]
								.getDeclaredAnnotations();
						methodAnnoMap.put(classMethods[i], a);
						Log.i("rtmap", "类名：" + c.getName() + "   方法名："
								+ classMethods[i].getName());
					}
					classMethodAnnoMap.put(c, methodAnnoMap);
				}
			}
			Log.i("rtmap", "长度为：" + classes.size());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
