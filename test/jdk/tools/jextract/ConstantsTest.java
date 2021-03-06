/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;
import jdk.incubator.foreign.GroupLayout;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SystemABI.Type;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/*
 * @test
 * @modules jdk.incubator.jextract
 * @build ConstantsTest
 * @run testng/othervm -Djdk.incubator.foreign.Foreign=permit ConstantsTest
 */
public class ConstantsTest extends JextractToolRunner {
    private Class<?> constants;
    private Path dirPath;
    private Loader loader;

    @BeforeTest
    public void setup() {
        dirPath = getOutputFilePath("ConstantsTest_output");
        run( "-d", dirPath.toString(), getInputFilePath("constants.h").toString()).checkSuccess();
        loader = classLoader(dirPath);
        constants = loader.loadClass("constants_h");
    }

    @AfterTest
    public void cleanup() {
        constants = null;
        loader.close();
        deleteDir(dirPath);
    }


    @Test(dataProvider = "definedConstants")
    public void checkConstantsSignatures(String name, Class<?> type, Object value) {
        var f = findField(constants, name);
        assertNotNull(f);
        assertTrue(f.getType() == type);
    }

    @Test(dataProvider = "definedConstants")
    public void checkConstantsValues(String name, Class<?> type, Predicate<Object> checker) throws ReflectiveOperationException {
        Object actual = findField(constants, name).get(null);
        assertTrue(checker.test(actual));
    }

    @Test(dataProvider = "missingConstants")
    public void checkMissingConstants(String name) {
        assertTrue(Stream.of(constants.getDeclaredFields())
                .noneMatch(m -> m.getName().equals(name)));
    }

    @DataProvider
    public static Object[][] definedConstants() {
        return new Object[][] {
                { "SUP", int.class, equalsTo(5) },
                { "ZERO", int.class, equalsTo(0) },
                { "ONE", int.class, equalsTo(1) },
                { "TWO", int.class, equalsTo(2) },
                { "THREE", int.class, equalsTo(3) },
                { "FOUR", long.class, equalsTo(4L) },
                { "FIVE", long.class, equalsTo(5L) },
                { "SIX", int.class, equalsTo(6) },
                { "FLOAT_VALUE", float.class, equalsTo(1.32f) },
                { "DOUBLE_VALUE", double.class, equalsTo(1.32) },
                { "CHAR_VALUE", int.class, equalsTo(104) }, //integer char constants have type int
                { "MULTICHAR_VALUE", int.class, equalsTo(26728) },  //integer char constants have type int
                { "BOOL_VALUE", byte.class, equalsTo((byte)1) },
                { "SUB", int.class, equalsTo( 7 ) }
        };
    }

    static Predicate<Object> equalsTo(Object that) {
        return o -> o.equals(that);
    }

    @DataProvider
    public static Object[][] missingConstants() {
        return new Object[][] {
                { "ID" },
                { "SUM" },
                { "BLOCK_BEGIN" },
                { "BLOCK_END" },
                { "INTEGER_MAX_VALUE" },
                { "CYCLIC_1" },
                { "CYCLIC_2" },
                { "UNUSED" },
                // pointer type values
                { "STR" },
                { "QUOTE" },
                { "ZERO_PTR" },
                { "F_PTR" }
        };
    }
}
