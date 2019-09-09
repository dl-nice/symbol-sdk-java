/*
 * Copyright 2019. NEM
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.nem.sdk.model.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nem.core.utils.HexEncoder;
import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.mosaic.MosaicNonce;
import io.nem.sdk.model.mosaic.MosaicProperties;
import io.nem.sdk.model.namespace.AliasAction;
import io.nem.sdk.model.namespace.NamespaceId;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AddressAliasTransactionTest {

    @Test
    void shouldSerialize() {

        NetworkType networkType = NetworkType.MIJIN_TEST;
        Integer version = 1;
        Deadline deadline = new Deadline(2, ChronoUnit.HOURS);
        BigInteger fee = BigInteger.ONE;
        NamespaceId namespaceId = new NamespaceId(new BigInteger("-8884663987180930485"));
        PublicAccount signature = PublicAccount.createFromPublicKey(
            "68b3fbb18729c1fde225c57f8ce080fa828f0067e451a3fd81fa628842b0b763",
            NetworkType.MIJIN_TEST);
        TransactionInfo transactionInfo =
            TransactionInfo.createAggregate(
                new BigInteger("121855"),
                1,
                "5A3D23889CD1E800015929A9",
                "3D28C804EDD07D5A728E5C5FFEC01AB07AFA5766AE6997B38526D36015A4D006",
                "5A0069D83F17CF0001777E55");

        AddressAliasTransaction transaction = new AddressAliasTransaction(networkType, version,
            deadline, fee, AliasAction.LINK, namespaceId, signature.getAddress(),
            Optional.of("signing"),
            Optional.of(signature), Optional.of(transactionInfo));

        Assertions.assertTrue(
            HexEncoder.getString(transaction.generateBytes())
                .startsWith("9a000000000000000000000000000000"));

        Assertions.assertTrue(
            HexEncoder.getString(transaction.generateEmbeddedBytes())
                .startsWith("4a00000068b3fbb18729c1fde225c57f8"));
    }
}