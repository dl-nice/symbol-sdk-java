/*
 * Copyright 2018 NEM
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

package io.nem.sdk.model.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModifyMultisigAccountTransactionTest {

    @Test
    void createAMultisigModificationTransactionViaConstructor() {
        ModifyMultisigAccountTransaction modifyMultisigAccountTransaction =
            ModifyMultisigAccountTransaction.create(
                new Deadline(2, ChronoUnit.HOURS),
                (byte) 2,
                (byte) 1,
                Collections.singletonList(
                    new MultisigCosignatoryModification(
                        MultisigCosignatoryModificationType.ADD,
                        PublicAccount.createFromPublicKey(
                            "68b3fbb18729c1fde225c57f8ce080fa828f0067e451a3fd81fa628842b0b763",
                            NetworkType.MIJIN_TEST))),
                NetworkType.MIJIN_TEST);

        assertEquals(NetworkType.MIJIN_TEST, modifyMultisigAccountTransaction.getNetworkType());
        assertTrue(1 == modifyMultisigAccountTransaction.getVersion());
        assertTrue(
            LocalDateTime.now()
                .isBefore(modifyMultisigAccountTransaction.getDeadline().getLocalDateTime()));
        assertEquals(BigInteger.valueOf(0), modifyMultisigAccountTransaction.getFee());
        assertEquals(2, modifyMultisigAccountTransaction.getMinApprovalDelta());
        assertEquals(1, modifyMultisigAccountTransaction.getMinRemovalDelta());
        assertEquals(
            "68b3fbb18729c1fde225c57f8ce080fa828f0067e451a3fd81fa628842b0b763".toUpperCase(),
            modifyMultisigAccountTransaction
                .getModifications()
                .get(0)
                .getCosignatoryPublicAccount()
                .getPublicKey()
                .toString()
                .toUpperCase());
        assertEquals(
            MultisigCosignatoryModificationType.ADD,
            modifyMultisigAccountTransaction.getModifications().get(0).getType());
    }

    @Test
    @DisplayName("Serialization")
    void serialization() {
        // Generated at nem2-library-js/test/transactions/ModifyMultisigAccountTransaction.spec.js
        String expected =
            "bd00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001905541000000000000000001000000000000000102020068b3fbb18729c1fde225c57f8ce080fa828f0067e451a3fd81fa628842b0b76300cf893ffcc47c33e7f68ab1db56365c156b0736824a0c1e273f9e00b8df8f01eb";
        ModifyMultisigAccountTransaction modifyMultisigAccountTransaction =
            ModifyMultisigAccountTransaction.create(
                new FakeDeadline(),
                (byte) 2,
                (byte) 1,
                Arrays.asList(
                    new MultisigCosignatoryModification(
                        MultisigCosignatoryModificationType.ADD,
                        PublicAccount.createFromPublicKey(
                            "68b3fbb18729c1fde225c57f8ce080fa828f0067e451a3fd81fa628842b0b763",
                            NetworkType.MIJIN_TEST)),
                    new MultisigCosignatoryModification(
                        MultisigCosignatoryModificationType.ADD,
                        PublicAccount.createFromPublicKey(
                            "cf893ffcc47c33e7f68ab1db56365c156b0736824a0c1e273f9e00b8df8f01eb",
                            NetworkType.MIJIN_TEST))),
                NetworkType.MIJIN_TEST);

        byte[] actual = modifyMultisigAccountTransaction.generateBytes();
        assertEquals(expected, Hex.toHexString(actual));
    }
}