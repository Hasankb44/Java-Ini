package com.github.ini;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IniArray {

    private final String path;
    private final File file;
    private StringBuilder builder = new StringBuilder();
    private Map<String, Object> map = new HashMap<>();
    private String currentSection = "";

    public IniArray(String path) throws IOException {
        this.path = path;
        this.file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(path + " is not found");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }

        initialize(); // Dosyayı okuduktan sonra içeriği başlat
    }

    public void initialize() {
        String content = builder.toString();
        String[] lines = content.split("\n");
        String section = "";

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim();
            } else if (line.contains("=")) {
                String[] kv = line.split("=", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String value = kv[1].trim();
                    map.put(section + "/" + key, value);
                }
            }
        }
    }

    public void addOrUpdateEntry(String section, String key, String value) {
        map.put(section + "/" + key, value);
    }

    public void save() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            Map<String, Map<String, String>> sections = new HashMap<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String[] parts = entry.getKey().split("/", 2);
                if (parts.length == 2) {
                    String section = parts[0];
                    String key = parts[1];
                    String value = (String) entry.getValue();

                    sections.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
                }
            }

            for (Map.Entry<String, Map<String, String>> sectionEntry : sections.entrySet()) {
                writer.write("[" + sectionEntry.getKey() + "]\n");
                for (Map.Entry<String, String> kvEntry : sectionEntry.getValue().entrySet()) {
                    writer.write(kvEntry.getKey() + "=" + kvEntry.getValue() + "\n");
                }
                writer.write("\n");
            }
        }
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
