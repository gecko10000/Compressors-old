package io.github.levtey.Compressors;

import redempt.redlib.config.annotations.ConfigName;

import java.util.Arrays;
import java.util.List;

public class Config {

    @ConfigName("item.name")
    static String itemName = "&e&lCompressor";

    @ConfigName("item.lore")
    static List<String> lore = Arrays.asList("&aCompresses ores and", "&avarious other blocks.");
}
