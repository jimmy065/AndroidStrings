package com.android.test.androidstrings.utils;

import junit.framework.TestCase;


public class GlobalizationUtilTest extends TestCase {

    public void testGetXlsData() {
        String path = "./test.xls";

        GlobalizationUtil.getXlsData(path, 1, 4);
    }


}