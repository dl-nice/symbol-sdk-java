/*
 * Copyright 2020 NEM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nem.symbol.core.crypto;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;

public abstract class CryptoEngineTest {

    @Test
    public void canGetCurve() {
        // Act:
        final Curve curve = this.getCryptoEngine().getCurve();

        // Assert:
        MatcherAssert.assertThat(curve, IsInstanceOf.instanceOf(Curve.class));
    }

    @Test
    public void canCreateDsaSigner() {
        // Act:
        final CryptoEngine engine = this.getCryptoEngine();
        final DsaSigner signer = engine
            .createDsaSigner(KeyPair.random(engine));

        // Assert:
        MatcherAssert.assertThat(signer, IsInstanceOf.instanceOf(DsaSigner.class));
    }

    @Test
    public void canCreateKeyGenerator() {
        // Act:
        final KeyGenerator keyGenerator = this.getCryptoEngine().createKeyGenerator();

        // Assert:
        MatcherAssert.assertThat(keyGenerator, IsInstanceOf.instanceOf(KeyGenerator.class));
    }

    @Test
    public void canCreateKeyAnalyzer() {
        // Act:
        final KeyAnalyzer keyAnalyzer = this.getCryptoEngine().createKeyAnalyzer();

        // Assert:
        MatcherAssert.assertThat(keyAnalyzer, IsInstanceOf.instanceOf(KeyAnalyzer.class));
    }

    @Test
    public void canCreateBlockCipher() {
        // Act:
        final CryptoEngine engine = this.getCryptoEngine();
        final BlockCipher blockCipher =
            engine.createBlockCipher(KeyPair.random(engine), KeyPair.random(engine));

        // Assert:
        MatcherAssert.assertThat(blockCipher, IsInstanceOf.instanceOf(BlockCipher.class));
    }

    protected abstract CryptoEngine getCryptoEngine();
}
