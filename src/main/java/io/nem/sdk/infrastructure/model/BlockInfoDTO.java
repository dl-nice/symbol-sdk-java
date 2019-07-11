/*
 * Copyright 2019 NEM
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
/*
 * Catapult REST API Reference
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 0.7.15
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package io.nem.sdk.infrastructure.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * BlockInfoDTO
 */
@javax.annotation.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2019-06-20T19:56:23.892+01:00[Europe/London]")
public class BlockInfoDTO {

    public static final String SERIALIZED_NAME_META = "meta";
    public static final String SERIALIZED_NAME_BLOCK = "block";
    @SerializedName(SERIALIZED_NAME_META)
    private BlockMetaDTO meta = null;
    @SerializedName(SERIALIZED_NAME_BLOCK)
    private BlockDTO block = null;

    public BlockInfoDTO meta(BlockMetaDTO meta) {
        this.meta = meta;
        return this;
    }

    /**
     * Get meta
     *
     * @return meta
     */
    @ApiModelProperty(required = true, value = "")
    public BlockMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(BlockMetaDTO meta) {
        this.meta = meta;
    }

    public BlockInfoDTO block(BlockDTO block) {
        this.block = block;
        return this;
    }

    /**
     * Get block
     *
     * @return block
     */
    @ApiModelProperty(required = true, value = "")
    public BlockDTO getBlock() {
        return block;
    }

    public void setBlock(BlockDTO block) {
        this.block = block;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockInfoDTO blockInfoDTO = (BlockInfoDTO) o;
        return Objects.equals(this.meta, blockInfoDTO.meta)
            && Objects.equals(this.block, blockInfoDTO.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meta, block);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BlockInfoDTO {\n");
        sb.append("    meta: ").append(toIndentedString(meta)).append("\n");
        sb.append("    block: ").append(toIndentedString(block)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first
     * line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}