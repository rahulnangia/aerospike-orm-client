package com.rn.aerospike.client.utils;

import com.aerospike.client.policy.WritePolicy;

/**
 * @author rahul
 */
public class PolicyHelper {

    public static WritePolicy getDefaultTTLWritePolicy(int ttl, WritePolicy policy) {
        WritePolicy ttlPolicy = new WritePolicy(policy);
        ttlPolicy.expiration = ttl;
        return ttlPolicy;
    }

}
