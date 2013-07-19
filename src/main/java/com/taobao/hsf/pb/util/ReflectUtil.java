package com.taobao.hsf.pb.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ReflectUtil
{

	public static boolean isPrimitive(Class clazz)
	{
		if (clazz.equals(Boolean.class) || clazz.equals(boolean.class))
		{
			return true;
		}
		else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
		{
			return true;
		}
		else if (clazz.equals(Short.class) || clazz.equals(short.class))
		{
			return true;
		}
		else if (clazz.equals(Integer.class) || clazz.equals(int.class))
		{
			return true;
		}
		else if (clazz.equals(Long.class) || clazz.equals(long.class))
		{
			return true;
		}
		else if (clazz.equals(Float.class) || clazz.equals(float.class))
		{
			return true;
		}
		else if (clazz.equals(Double.class) || clazz.equals(double.class))
		{
			return true;
		}
		else if (clazz.equals(Character.class) || clazz.equals(char.class))
		{
			return true;
		}
		else if (clazz.equals(String.class))
		{
			return true;
		}

		return false;
	}

	public static HashMap<String, Method> getSetterMethodMap(Class cl, boolean checkGetter)
	{
		HashMap<String, Method> methodMap = new HashMap<String, Method>();

		for (; cl != null; cl = cl.getSuperclass())
		{
			Method[] methods = cl.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++)
			{
				Method method = methods[i];

				if (Modifier.isStatic(method.getModifiers()))
					continue;

				String name = method.getName();
				if (!((name.startsWith("set")) || (name.startsWith("add"))))
					continue;

				Class[] paramTypes = method.getParameterTypes();
				if (!(paramTypes.length == 1))
					continue;

				// if (!method.getReturnType().equals(void.class))
				// continue;
				if (checkGetter && (findGetter(methods, name, paramTypes[0]) == null))
					continue;

				// XXX: could parameterize the handler to only deal with public
				try
				{
					method.setAccessible(true);
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}

				name = name.substring(3);

				int j = 0;
				for (; j < name.length() && Character.isUpperCase(name.charAt(j)); j++)
				{
				}

				if (j == 1)
					name = name.substring(0, j).toLowerCase() + name.substring(j);
				else if (j > 1)
					name = name.substring(0, j - 1).toLowerCase() + name.substring(j - 1);

				methodMap.put(name, method);
			}
		}

		return methodMap;
	}

	public static HashMap<String, Method> getGetterMethodMap(Class cl, boolean checkSetter)
	{
		HashMap<String, Method> methodMap = new HashMap<String, Method>();

		for (; cl != null; cl = cl.getSuperclass())
		{
			Method[] methods = cl.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++)
			{
				Method method = methods[i];

				if (Modifier.isStatic(method.getModifiers()))
					continue;

				String name = method.getName();

				if (!name.startsWith("get"))
					continue;

				Class[] paramTypes = method.getParameterTypes();
				if (paramTypes.length != 0)
					continue;

				if (method.getReturnType().equals(void.class))
					continue;

				if (checkSetter && !(findSetter(methods, name, method.getReturnType()) == null))
					continue;

				// XXX: could parameterize the handler to only deal with public
				try
				{
					method.setAccessible(true);
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}

				name = name.substring(3);

				int j = 0;
				for (; j < name.length() && Character.isUpperCase(name.charAt(j)); j++)
				{
				}

				if (j == 1)
					name = name.substring(0, j).toLowerCase() + name.substring(j);
				else if (j > 1)
					name = name.substring(0, j - 1).toLowerCase() + name.substring(j - 1);

				methodMap.put(name, method);
			}
		}

		return methodMap;
	}

	public static Method findGetter(Method[] methods, String setterName, Class arg)
	{
		String getterName = "get" + setterName.substring(3);

		for (int i = 0; i < methods.length; i++)
		{
			Method method = methods[i];

			if (!method.getName().equals(getterName))
				continue;

			if (!method.getReturnType().equals(arg))
				continue;

			Class[] params = method.getParameterTypes();

			if (params.length == 0)
				return method;
		}

		return null;
	}

	public static Method findSetter(Method[] methods, String setterName, Class arg)
	{
		String getterName = "set" + setterName.substring(3);

		for (int i = 0; i < methods.length; i++)
		{
			Method method = methods[i];

			if (!method.getName().equals(getterName))
				continue;

			if (!method.getReturnType().equals(arg))
				continue;

			Class[] params = method.getParameterTypes();

			if (params.length == 0)
				return method;
		}

		return null;
	}

	public static Class getPriClass(Class<? extends Object> clazz)
	{
		if (clazz.equals(Boolean.class) || clazz.equals(boolean.class))
		{
			return boolean.class;
		}
		else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
		{
			return byte.class;
		}
		else if (clazz.equals(Short.class) || clazz.equals(short.class))
		{
			return short.class;
		}
		else if (clazz.equals(Integer.class) || clazz.equals(int.class))
		{
			return int.class;
		}
		else if (clazz.equals(Long.class) || clazz.equals(long.class))
		{
			return long.class;
		}
		else if (clazz.equals(Float.class) || clazz.equals(float.class))
		{
			return float.class;
		}
		else if (clazz.equals(Double.class) || clazz.equals(double.class))
		{
			return double.class;
		}
		else if (clazz.equals(Character.class) || clazz.equals(char.class))
		{
			return char.class;
		}
		return clazz;
	}

	private static final Class<? extends Annotation> s_javaxTransient;

	static
	{
		Class<? extends Annotation> javaxTransient = null;
		try
		{
			javaxTransient = (Class<? extends Annotation>) Class.forName("javax.persistence.Transient");
		}
		catch (ClassNotFoundException e)
		{
			// ignore
		}
		s_javaxTransient = javaxTransient;
	}

	public static boolean hasTransientAnnotation(final AnnotatedElement element)
	{
		if (s_javaxTransient != null)
		{
			if (element.getAnnotation(s_javaxTransient) != null)
				return true;
		}
		return false;
	}

}
