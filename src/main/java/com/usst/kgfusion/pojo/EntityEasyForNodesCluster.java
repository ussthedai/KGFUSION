package com.usst.kgfusion.pojo;

public class EntityEasyForNodesCluster {

    private Long id;
    private String name;
    private String ontologySmbol;
    private String entityType;
    private String subOntologySymbol;
    private String entitySubClass;


    // constructor
    public EntityEasyForNodesCluster() {
    }

    /// parameterized constructor
    public EntityEasyForNodesCluster(Long id, String name, String ontologySmbol, String entityType, String subOntologySymbol, String entitySubClass) {
        this.id = id;
        this.name = name;
        this.ontologySmbol = ontologySmbol;
        this.entityType = entityType;
        this.subOntologySymbol = subOntologySymbol;
        this.entitySubClass = entitySubClass;
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
    
    public String getSubOntologySymbol() {
        return subOntologySymbol;
    }
    public void setSubOntologySymbol(String subOntologySymbol) {
        this.subOntologySymbol = subOntologySymbol;
    }
    public String getEntitySubClass() {
        return entitySubClass;
    }
    public void setEntitySubClass(String entitySubClass) {
        this.entitySubClass = entitySubClass;
    }

    
}
