package com.chinashuguo.project.ldap;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

/**
 * Created by kui.li on 2014/9/2.
 */
public class LDAPControl {
    public static boolean connect(String username,String password) {
        boolean rtnVal = false;
        DirContext ctx=null;
        Hashtable<String,String> HashEnv = new Hashtable<String,String>();
        HashEnv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory"); // LDAP工厂�?
        HashEnv.put(Context.PROVIDER_URL, "ldap://192.168.160.5:389");// 默认端口389
        HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别(none,simple,strong)
        HashEnv.put(Context.SECURITY_PRINCIPAL, username); //AD的用户名
        HashEnv.put(Context.SECURITY_CREDENTIALS, password); //AD的密�?
//        HashEnv.put(com.sun.jndi.ldap..timeout, 3000);//连接超时设置�?3�?
        try {
            ctx = new InitialLdapContext(HashEnv,null);// 初始化上下文
            System.out.println("身份验证成功!");
            rtnVal = true;
        } catch (AuthenticationException e) {
            System.out.println("身份验证失败!");
            //e.printStackTrace();
        } catch (javax.naming.CommunicationException e) {
            System.out.println("AD域连接失�?!");
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println("身份验证未知异常!");
            //e.printStackTrace();
        } finally{
            if(null!=ctx){
                try {
                    ctx.close();
                    ctx=null;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
        return rtnVal;
    }

    static int totalResults = 0;
    public static void main(String[] args) {
        boolean rtnVal = LDAPControl.connect("ragentekxian\\kui.li", "Ragentek!");
        System.out.println("rtnVal=" + rtnVal);
        
        SearchControls searchCtls  = new SearchControls ();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = "objectClass=User";
        String searchBase = "DC=cn";
        String returnedAtts[] = {
  	          "url", "whenChanged", "employeeID", "name", "userPrincipalName",
  	          "physicalDeliveryOfficeName", "departmentNumber", "telephoneNumber",
  	          "homePhone", "mobile", "department", "sAMAccountName", "whenChanged",
  	          "mail"}; 
        searchCtls.setReturningAttributes(returnedAtts);
        
        DirContext ctx=null;
        Hashtable<String,String> HashEnv = new Hashtable<String,String>();
        HashEnv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory"); // LDAP工厂�?
        HashEnv.put(Context.PROVIDER_URL, "ldap://192.168.160.5:389");// 默认端口389
        HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别(none,simple,strong)
        HashEnv.put(Context.SECURITY_PRINCIPAL, "ragentekxian\\kui.li"); //AD的用户名
        HashEnv.put(Context.SECURITY_CREDENTIALS, "Ragentek!"); //AD的密�?
//        HashEnv.put(com.sun.jndi.ldap..timeout, 3000);//连接超时设置�?3�?
        try {
            ctx = new InitialLdapContext(HashEnv,null);// 初始化上下文
            NamingEnumeration answer = ctx.search(searchBase, searchFilter,searchCtls);
            System.out.println("ddd="+answer.hasMoreElements());
            while (answer.hasMoreElements()) {
    	        SearchResult sr = (SearchResult) answer.next();
    	        System.out.println("************************************************");
    	        System.out.println(sr.getName());
     
    	        Attributes Attrs = sr.getAttributes();
    	        if (Attrs != null) {
    	          try {
    	            for (NamingEnumeration ne = Attrs.getAll(); ne.hasMore(); ) {
    	              Attribute Attr = (Attribute) ne.next();
     
    	              System.out.println(" AttributeID=" + Attr.getID().toString());
     
    	              //读取属性值
    	              for (NamingEnumeration e = Attr.getAll(); e.hasMore();totalResults++) {
    	                System.out.println("    AttributeValues=" + e.next().toString());
    	              }
    	              System.out.println("    ---------------");
     
    	              //读取属性值
    	              Enumeration values = Attr.getAll();
    	              if (values != null) { // 迭代
    	                while (values.hasMoreElements()) {
    	                  System.out.println("    AttributeValues=" + values.nextElement());
    	                }
    	              }
    	              System.out.println("    ---------------");
    	            }
    	          }
    	          catch (NamingException e) {
    	            System.err.println("Throw Exception : " + e);
    	          }
    	        }
    	      }
    	      System.out.println("Number: " + totalResults);
    	      ctx.close();
        } catch(Exception e) {
        	
        }

    }
}
