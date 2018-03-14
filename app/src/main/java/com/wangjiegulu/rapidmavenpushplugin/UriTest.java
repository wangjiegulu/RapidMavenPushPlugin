package com.wangjiegulu.rapidmavenpushplugin;

import java.net.URI;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 14/03/2018.
 */
public class UriTest {
    public static void main(String[] args) {
        System.out.println("scheme: " + (null == URI.create("/resp/a/b/c").getScheme()));
        System.out.println("scheme: " + URI.create("http://resp/a/b/c").getScheme());
        System.out.println("scheme: " + URI.create("file:///resp/a/b/c").getScheme());
    }
}
