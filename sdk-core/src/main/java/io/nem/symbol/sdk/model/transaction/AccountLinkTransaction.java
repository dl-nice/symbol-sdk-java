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

package io.nem.symbol.sdk.model.transaction;

import io.nem.symbol.sdk.model.account.PublicAccount;

/**
 *
 */
public class AccountLinkTransaction extends Transaction {

    private final PublicAccount remoteAccount;

    private final AccountLinkAction linkAction;

    public AccountLinkTransaction(AccountLinkTransactionFactory factory) {
        super(factory);
        this.remoteAccount = factory.getRemoteAccount();
        this.linkAction = factory.getLinkAction();
    }

    /**
     * Gets the public key.
     *
     * @return Public key.
     */
    public PublicAccount getRemoteAccount() {
        return remoteAccount;
    }

    /**
     * Gets the link action.
     *
     * @return Link action.
     */
    public AccountLinkAction getLinkAction() {
        return linkAction;
    }

}
