package com.searly.taxcontrol.sii.util;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.transforms.Transform;

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
                Transform.register(org.apache.xml.security.transforms.Transforms.TRANSFORM_C14N_OMIT_COMMENTS, TransformC14NChilkatHybrid.class);
            } catch (Exception ignored) {
            }

            try {
                Canonicalizer.register(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS, CanonicalizerChilkatHybrid.class);
            } catch (Exception ignored) {
            }

            INSTALLED = true;
        }
    }
}
