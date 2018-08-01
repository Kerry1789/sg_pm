/**
 * 
 */
package com.chinashuguo.project.utils;

/**
 * @author kui.li
 *
 */
public class StringUtils {

	/**
	 * check str weather empty !
	 * @param str
	 * @return empty return false, else return true
	 */
	public static boolean isNotEmpty(String str){
		if(str != null && !"".equals(str))
			return true;
		return false;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
