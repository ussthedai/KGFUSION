package com.usst.kgfusion.pojo;

public class EntityEasy {

    private Long id;
    private String name;
    private String ontologySmbol;
    private String entityType;

    // constructor
    public EntityEasy() {
    }

    /// parameterized constructor
    public EntityEasy(Long id, String name, String ontologySmbol, String entityType) {
        this.id = id;
        this.name = name;
        this.ontologySmbol = ontologySmbol;
        this.entityType = entityType;
    }

    // getter and setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getOntologySmbol() {
        return ontologySmbol;
    }
    public void setOntologySmbol(String ontologySmbol) {
        this.ontologySmbol = ontologySmbol;
    }
    public String getEntityType() {
        return entityType;
    }
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    
}
