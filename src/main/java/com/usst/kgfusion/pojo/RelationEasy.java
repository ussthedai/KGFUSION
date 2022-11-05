package com.usst.kgfusion.pojo;

public class RelationEasy {
    private Long id;
    private String name;
    private String ralationType;
    private String ontologySymbol;
    private String relationSubType;
    private String subOntologySymbol;

    public RelationEasy(Long id, String name, String ralationType, String ontologySymbol, String relationSubType, String subOntologySymbol) {
        this.id = id;
        this.name = name;
        this.ralationType = ralationType;
        this.ontologySymbol = ontologySymbol;
        this.relationSubType = relationSubType;
        this.subOntologySymbol = subOntologySymbol;
    }

    public RelationEasy(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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

    public String getRalationType() {
        return ralationType;
    }

    public void setRalationType(String ralationType) {
        this.ralationType = ralationType;
    }

    public String getOntologySymbol() {
        return ontologySymbol;
    }

    public void setOntologySymbol(String ontologySymbol) {
        this.ontologySymbol = ontologySymbol;
    }

    public String getRelationSubType() {
        return relationSubType;
    }

    public void setRelationSubType(String relationSubType) {
        this.relationSubType = relationSubType;
    }

    public String getSubOntologySymbol() {
        return subOntologySymbol;
    }

    public void setSubOntologySymbol(String subOntologySymbol) {
        this.subOntologySymbol = subOntologySymbol;
    }
}
