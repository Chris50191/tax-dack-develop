package com.searly.taxcontrol.sii.util;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;

import java.util.Map;

public final class SantuarioChilkatCompatInstaller {

    private static volatile boolean INSTALLED = false;

    private SantuarioChilkatCompatInstaller() {
    }

    public static void install() {
        if (INSTALLED) {
            return;
        }
        synchronized (SantuarioChilkatCompatInstaller.class) {
            if (INSTALLED) {
                return;
            }

            try {
                Init.init();
            } catch (Exception ignored) {
            }

            try {
                String c14nUri = org.apache.xml.security.transforms.Transforms.TRANSFORM_C14N_OMIT_COMMENTS;
                Transform.register(c14nUri, TransformC14NChilkatHybrid.class);
                Transform.register(TransformC14NChilkatHybrid.URI, TransformC14NChilkatHybrid.class);
            } catch (Exception ignored) {
            }

            try {
                java.lang.reflect.Field f = Transform.class.getDeclaredField("transformSpiHash");
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) v;
                    TransformSpi spi = null;
                    try {
                        Object existing = map.get(TransformC14NChilkatHybrid.URI);
                        if (existing instanceof TransformSpi) {
                            spi = new TransformC14NChilkatHybrid();
                        } else if (existing instanceof Class) {
                            Class<?> clz = (Class<?>) existing;
                            Object inst = clz.getDeclaredConstructor().newInstance();
                            if (inst instanceof TransformSpi) {
                                spi = (TransformSpi) inst;
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    if (spi == null) {
                        spi = new TransformC14NChilkatHybrid();
                    }

                    String c14nUri = org.apache.xml.security.transforms.Transforms.TRANSFORM_C14N_OMIT_COMMENTS;
                    map.put(c14nUri, spi);
                    map.put(TransformC14NChilkatHybrid.URI, spi);

                    boolean debug = Boolean.parseBoolean(System.getProperty("hybrid.debug", "false"));
                    if (debug) {
                        Object vv = map.get(c14nUri);
                        System.out.println("[hybrid] Installer: transformSpiHash[" + c14nUri + "]=" + (vv == null ? "null" : vv.getClass().getName()));
                    }
                }
            } catch (Exception ignored) {
            }

            try {
                Canonicalizer.register(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS, CanonicalizerChilkatHybrid.class);
            } catch (Exception ignored) {
            }

            try {
                java.lang.reflect.Field f = Canonicalizer.class.getDeclaredField("canonicalizerHash");
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Class<?>> map = (Map<String, Class<?>>) v;
                    map.put(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS, CanonicalizerChilkatHybrid.class);
                    map.put(CanonicalizerChilkatHybrid.URI, CanonicalizerChilkatHybrid.class);

                    boolean debug = Boolean.parseBoolean(System.getProperty("hybrid.debug", "false"));
                    if (debug) {
                        Class<?> cc = map.get(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
                        System.out.println("[hybrid] Installer: canonicalizerHash[" + Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS + "]=" + (cc == null ? "null" : cc.getName()));
                    }
                }
            } catch (Exception ignored) {
            }

            INSTALLED = true;
        }
    }
}
