package com.slotsense.core;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class RNG {
    public final long seed;
    private final Random random;
    public final boolean debug;

    public final List<Map<String, Object>> trace = new ArrayList<>();
    public final Map<String, Long> forks = new HashMap<>();

    public RNG(long seed, boolean debug) {
        this.seed = seed;
        this.random = new Random(seed);
        this.debug = debug;
    }

    private <T> T record(String label, T value) {
        if (debug) {
            Map<String, Object> e = new HashMap<>();
            e.put("label", label);
            e.put("value", value);
            trace.add(e);
        }
        return value;
    }

    public double rand(String label) {
        return record(label, random.nextDouble());
    }

    public int randint(int a, int b, String label) {
        // inclusivo [a,b]
        int v = a + random.nextInt((b - a) + 1);
        return record(label, v);
    }

    public <T> T choice(List<T> seq, String label) {
        if (seq == null || seq.isEmpty()) throw new IllegalArgumentException("choice() seq vacía");
        int idx = randint(0, seq.size() - 1, label + ".idx");
        return record(label, seq.get(idx));
    }

    // seq: lista de pares (valor, peso)
    public <T> T weightedChoice(List<Pair<T, Double>> seq, String label) {
        double total = 0;
        for (Pair<T, Double> p : seq) total += p.right;

        double pick = rand(label + ".pick") * total;
        double acc = 0;

        for (Pair<T, Double> p : seq) {
            acc += p.right;
            if (pick < acc) return record(label, p.left);
        }
        return record(label, seq.get(seq.size() - 1).left);
    }

    public RNG fork(String tag) {
        long newSeed = hashToLong(seed + "|" + tag);
        RNG child = new RNG(newSeed, debug);

        if (debug) {
            forks.put(tag, newSeed);
            // compartimos traza conceptualmente (igual que en Python)
            child.trace.clear();
            child.trace.addAll(this.trace);
            child.forks.putAll(this.forks);
        }
        return child;
    }

    private static long hashToLong(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            // tomar 8 bytes para long
            return ByteBuffer.wrap(dig, 0, 8).getLong();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // util
    public static class Pair<L, R> {
        public final L left;
        public final R right;
        public Pair(L left, R right) { this.left = left; this.right = right; }
    }
}
