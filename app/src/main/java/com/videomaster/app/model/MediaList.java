package com.videomaster.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user-created playlist containing audio/video items.
 */
public class MediaList {

    private String       id;
    private String       name;
    private String       category;
    private List<String> itemUris;   // ordered list of URI strings
    private long         createdAt;

    public MediaList() {
        this.id        = UUID.randomUUID().toString();
        this.itemUris  = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public MediaList(String name, String category) {
        this();
        this.name     = name;
        this.category = category;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getId()                    { return id; }
    public void   setId(String id)           { this.id = id; }

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public String getCategory()              { return category; }
    public void   setCategory(String c)      { this.category = c; }

    public List<String> getItemUris()        { return itemUris; }
    public void setItemUris(List<String> u)  { this.itemUris = u; }

    public long getCreatedAt()               { return createdAt; }
    public void setCreatedAt(long t)         { this.createdAt = t; }

    // ── Helpers ────────────────────────────────────────────────────────────

    public void addItem(String uri) {
        if (!itemUris.contains(uri)) itemUris.add(uri);
    }

    public void removeItem(String uri) {
        itemUris.remove(uri);
    }

    public int size() { return itemUris.size(); }
}
