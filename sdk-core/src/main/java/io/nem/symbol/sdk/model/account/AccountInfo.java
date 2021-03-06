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

package io.nem.symbol.sdk.model.account;

import io.nem.symbol.sdk.model.mosaic.Mosaic;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The account info structure describes basic information for an account.
 *
 * @since 1.0
 */
public class AccountInfo {

    private final Address address;
    private final BigInteger addressHeight;
    private final String publicKey;
    private final BigInteger publicKeyHeight;
    private final List<Importances> importances;
    private final List<Mosaic> mosaics;
    private final AccountType accountType;

    @SuppressWarnings("squid:S00107")
    public AccountInfo(
        Address address,
        BigInteger addressHeight,
        String publicKey,
        BigInteger publicKeyHeight,
        BigInteger importance,
        BigInteger importanceHeight,
        List<Mosaic> mosaics,
        AccountType accountType) {
        this.address = address;
        this.addressHeight = addressHeight;
        this.publicKey = publicKey;
        this.publicKeyHeight = publicKeyHeight;
        this.accountType = accountType;
        this.importances = new ArrayList<>();
        this.importances.add(new Importances(importance, importanceHeight));
        this.mosaics = mosaics;
    }

    /**
     * Returns account address.
     *
     * @return {@link Address}
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Returns height when the address was published.
     *
     * @return BigInteger
     */
    public BigInteger getAddressHeight() {
        return addressHeight;
    }

    /**
     * Returns public key of the account.
     *
     * @return String
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Returns height when the public key was published.
     *
     * @return BigInteger
     */
    public BigInteger getPublicKeyHeight() {
        return publicKeyHeight;
    }

    /**
     * Returns list of importances for the account.
     *
     * @return BigInteger
     */
    public List<Importances> getImportances() {
        return importances;
    }

    /**
     * Returns mosaics hold by the account.
     *
     * @return List of {@link Mosaic}
     */
    public List<Mosaic> getMosaics() {
        return mosaics;
    }

    /**
     * Returns height when the address was published.
     *
     * @return {@link PublicAccount}
     */
    public PublicAccount getPublicAccount() {
        return PublicAccount.createFromPublicKey(this.publicKey, this.address.getNetworkType());
    }

    /**
     * @return the account type.
     */
    public AccountType getAccountType() {
        return accountType;
    }
}
