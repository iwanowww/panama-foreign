/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */

/*
 * @test
 * @modules jdk.incubator.vector
 * @run testng Int128VectorLoadStoreTests
 *
 */

// -- This file was mechanically generated: Do not edit! -- //

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.IntVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.function.IntFunction;

@Test
public class Int128VectorLoadStoreTests extends AbstractVectorTest {
    static final VectorSpecies<Integer> SPECIES =
                IntVector.SPECIES_128;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 10);


    static final int BUFFER_REPS = Integer.getInteger("jdk.incubator.vector.test.buffer-vectors", 25000 / 128);

    static final int BUFFER_SIZE = Integer.getInteger("jdk.incubator.vector.test.buffer-size", BUFFER_REPS * (128 / 8));

    static void assertArraysEquals(int[] a, int[] r, boolean[] mask) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(mask[i % SPECIES.length()] ? a[i] : (int) 0, r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(mask[i % SPECIES.length()] ? a[i] : (int) 0, r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(int[] a, int[] r, int[] im) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(a[im[i]], r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(a[im[i]], r[i], "at index #" + i);
        }
    }

    static void assertArraysEquals(int[] a, int[] r, int[] im, boolean[] mask) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(mask[i % SPECIES.length()] ? a[im[i]] : (int) 0, r[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(mask[i % SPECIES.length()] ? a[im[i]] : (int) 0, r[i], "at index #" + i);
        }
    }

    static final List<IntFunction<int[]>> INT_GENERATORS = List.of(
            withToString("int[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (int)(i * 5));
            }),
            withToString("int[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((int)(i + 1) == 0) ? 1 : (int)(i + 1)));
            })
    );

    @DataProvider
    public Object[][] intProvider() {
        return INT_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intIndexMapProvider() {
        return INDEX_GENERATORS.stream().
                flatMap(fim -> INT_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fim};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intIndexMapMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INDEX_GENERATORS.stream().
                    flatMap(fim -> INT_GENERATORS.stream().map(fa -> {
                        return new Object[] {fa, fim, fm};
                }))).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intByteBufferProvider() {
        return INT_GENERATORS.stream().
                flatMap(fa -> BYTE_BUFFER_GENERATORS.stream().map(fb -> {
                    return new Object[]{fa, fb};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] intByteBufferMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INT_GENERATORS.stream().
                        flatMap(fa -> BYTE_BUFFER_GENERATORS.stream().map(fb -> {
                            return new Object[]{fa, fb, fm};
                        }))).
                toArray(Object[][]::new);
    }

    static ByteBuffer toBuffer(int[] a, IntFunction<ByteBuffer> fb) {
        ByteBuffer bb = fb.apply(a.length * SPECIES.elementSize() / 8);
        for (int v : a) {
            bb.putInt(v);
        }
        return bb.clear();
    }

    static int[] bufferToArray(ByteBuffer bb) {
        IntBuffer db = bb.asIntBuffer();
        int[] d = new int[db.capacity()];
        db.get(d);
        return d;
    }

    interface ToIntF {
        int apply(int i);
    }

    static int[] fill(int s , ToIntF f) {
        return fill(new int[s], f);
    }

    static int[] fill(int[] a, ToIntF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    @Test(dataProvider = "intProvider")
    static void loadStoreArray(IntFunction<int[]> fa) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i);
            }
        }
        Assert.assertEquals(a, r);
    }

    @Test(dataProvider = "intMaskProvider")
    static void loadStoreMaskArray(IntFunction<int[]> fa,
                                   IntFunction<boolean[]> fm) {
        int[] a = fa.apply(SPECIES.length());
        int[] r = new int[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i, vmask);
                av.intoArray(r, i);
            }
        }
        assertArraysEquals(a, r, mask);

        r = new int[a.length];
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                IntVector av = IntVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, vmask);
            }
        }

        assertArraysEquals(a, r, mask);
    }

    @Test(dataProvider = "intMaskProvider")
    static void loadStoreMask(IntFunction<int[]> fa,
                              IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = new boolean[mask.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Integer> vmask = VectorMask.fromArray(SPECIES, mask, i);
                vmask.intoArray(r, i);
            }
        }
        Assert.assertEquals(mask, r);
    }

    @Test(dataProvider = "intByteBufferProvider")
    static void loadStoreByteBuffer(IntFunction<int[]> fa,
                                    IntFunction<ByteBuffer> fb) {
        ByteBuffer a = toBuffer(fa.apply(SPECIES.length()), fb);
        ByteBuffer r = fb.apply(a.limit());

        int l = a.limit();
        int s = SPECIES.length() * SPECIES.elementSize() / 8;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < l; i += s) {
                IntVector av = IntVector.fromByteBuffer(SPECIES, a, i, ByteOrder.LITTLE_ENDIAN);
                av.intoByteBuffer(r, i, ByteOrder.LITTLE_ENDIAN);
            }
        }
        Assert.assertEquals(a.position(), 0, "Input buffer position changed");
        Assert.assertEquals(a.limit(), l, "Input buffer limit changed");
        Assert.assertEquals(r.position(), 0, "Result buffer position changed");
        Assert.assertEquals(r.limit(), l, "Result buffer limit changed");
        Assert.assertEquals(a, r, "Buffers not equal");
    }

    @Test(dataProvider = "intByteBufferProvider")
    static void loadReadOnlyStoreByteBuffer(IntFunction<int[]> fa,
                                            IntFunction<ByteBuffer> fb) {
        ByteBuffer a = toBuffer(fa.apply(SPECIES.length()), fb);
        a = a.asReadOnlyBuffer().order(a.order());
        ByteBuffer r = fb.apply(a.limit());

        int l = a.limit();
        int s = SPECIES.length() * SPECIES.elementSize() / 8;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < l; i += s) {
                IntVector av = IntVector.fromByteBuffer(SPECIES, a, i, ByteOrder.LITTLE_ENDIAN);
                av.intoByteBuffer(r, i, ByteOrder.LITTLE_ENDIAN);
            }
        }
        Assert.assertEquals(a.position(), 0, "Input buffer position changed");
        Assert.assertEquals(a.limit(), l, "Input buffer limit changed");
        Assert.assertEquals(r.position(), 0, "Result buffer position changed");
        Assert.assertEquals(r.limit(), l, "Result buffer limit changed");
        Assert.assertEquals(a, r, "Buffers not equal");
    }

    @Test(dataProvider = "intByteBufferMaskProvider")
    static void loadStoreByteBufferMask(IntFunction<int[]> fa,
                                        IntFunction<ByteBuffer> fb,
                                        IntFunction<boolean[]> fm) {
        ByteBuffer a = toBuffer(fa.apply(SPECIES.length()), fb);
        ByteBuffer r = fb.apply(a.limit());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        int l = a.limit();
        int s = SPECIES.length() * SPECIES.elementSize() / 8;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < l; i += s) {
                IntVector av = IntVector.fromByteBuffer(SPECIES, a, i, ByteOrder.LITTLE_ENDIAN, vmask);
                av.intoByteBuffer(r, i, ByteOrder.LITTLE_ENDIAN);
            }
        }
        Assert.assertEquals(a.position(), 0, "Input buffer position changed");
        Assert.assertEquals(a.limit(), l, "Input buffer limit changed");
        Assert.assertEquals(r.position(), 0, "Result buffer position changed");
        Assert.assertEquals(r.limit(), l, "Result buffer limit changed");
        assertArraysEquals(bufferToArray(a), bufferToArray(r), mask);

        a = toBuffer(fa.apply(SPECIES.length()), fb);
        r = fb.apply(a.limit());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < l; i += s) {
                IntVector av = IntVector.fromByteBuffer(SPECIES, a, i, ByteOrder.LITTLE_ENDIAN);
                av.intoByteBuffer(r, i, ByteOrder.LITTLE_ENDIAN, vmask);
            }
        }
        Assert.assertEquals(a.position(), 0, "Input buffer position changed");
        Assert.assertEquals(a.limit(), l, "Input buffer limit changed");
        Assert.assertEquals(r.position(), 0, "Result buffer position changed");
        Assert.assertEquals(r.limit(), l, "Result buffer limit changed");
        assertArraysEquals(bufferToArray(a), bufferToArray(r), mask);
    }

    @Test(dataProvider = "intByteBufferMaskProvider")
    static void loadReadOnlyStoreByteBufferMask(IntFunction<int[]> fa,
                                                IntFunction<ByteBuffer> fb,
                                                IntFunction<boolean[]> fm) {
        ByteBuffer a = toBuffer(fa.apply(SPECIES.length()), fb);
        a = a.asReadOnlyBuffer().order(a.order());
        ByteBuffer r = fb.apply(a.limit());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Integer> vmask = VectorMask.fromValues(SPECIES, mask);

        int l = a.limit();
        int s = SPECIES.length() * SPECIES.elementSize() / 8;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < l; i += s) {
                IntVector av = IntVector.fromByteBuffer(SPECIES, a, i, ByteOrder.LITTLE_ENDIAN, vmask);
                av.intoByteBuffer(r, i, ByteOrder.LITTLE_ENDIAN);
            }
        }
        Assert.assertEquals(a.position(), 0, "Input buffer position changed");
        Assert.assertEquals(a.limit(), l, "Input buffer limit changed");
        Assert.assertEquals(r.position(), 0, "Result buffer position changed");
        Assert.assertEquals(r.limit(), l, "Result buffer limit changed");
        assertArraysEquals(bufferToArray(a), bufferToArray(r), mask);
    }
}
