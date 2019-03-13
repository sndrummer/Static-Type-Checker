package edu.byu.yc.symboltable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Samuel Nuttall
 * <p>
 * ClassFieldsMethodsParams contains an individual class's fields, methods, and params types and names
 * This class is an integral part of the symbol table and holds all the relevent information for an
 * a class that has been visited by the symbol table visitor.
 */
public class ClassFieldsMethodsParams {

    private String classFQN;

    private List<ASTNameType> fields;
    private Map<ASTNameType, List<ASTNameType>> methodParamsMap;

    /**
     * Constructor for the class fields, methods, and parameters
     *
     * @param classFQN the fully qualified class name of the class
     */
    public ClassFieldsMethodsParams(String classFQN) {
        this.classFQN = classFQN;
        fields = new ArrayList<>();

        methodParamsMap = new HashMap<>();

    }

    /**
     * Returns the field type that matches the given field name
     *
     * @param fieldName String Name of the field
     * @return String representation of the type of the field that matches the fieldName param
     */
    public String getFieldTypeByName(String fieldName) {
        String fieldType = null;
        for (ASTNameType nameType : fields) {
            if (nameType.getName().equals(fieldName))
                fieldType = nameType.getType();
        }
        return fieldType;
    }

    /**
     * Returns the method type that matches the given method name
     *
     * @param methodName String name of the method
     * @return String representation of the return type of the method
     */
    public String getMethodTypeByName(String methodName) {
        String methodType = null;
        for (Map.Entry<ASTNameType, List<ASTNameType>> entry : methodParamsMap.entrySet()) {
            if (entry.getKey().getName().equals(methodName))
                methodType = entry.getKey().getType();
        }
        return methodType;
    }


    /**
     * Returns the type of the parameter that matches the given method name and parameter name
     *
     * @param methodName String name of the method
     * @param paramName  String name of the parameter
     * @return String representation of the type of the parameter
     */
    public String getParamTypeByName(String methodName, String paramName) {
        String type = null;
        List<ASTNameType> params = null;
        for (Map.Entry<ASTNameType, List<ASTNameType>> entry : methodParamsMap.entrySet()) {
            if (entry.getKey().getName().equals(methodName))
                params = entry.getValue();
        }

        if (params == null) return null;
        for (ASTNameType nameType : params) {
            if (nameType.getName().equals(paramName))
                type = nameType.getType();
        }
        return type;
    }

    public String getClassFQN() {
        return classFQN;
    }

    public List<ASTNameType> getFields() {
        return fields;
    }

    public Map<ASTNameType, List<ASTNameType>> getMethodParamsMap() {
        return methodParamsMap;
    }

    public void addField(ASTNameType fieldNameTypes) {
        fields.add(fieldNameTypes);
    }

    public void addMethod(ASTNameType methodNameType, List<ASTNameType> paramNameTypes) {
        methodParamsMap.put(methodNameType, paramNameTypes);
    }

    @Override
    public String toString() {
        return "\tClassFQN='" + classFQN + '\'' +
                ", \n\tFields=" + fields +
                ", \n\tMethodParams=" + methodParamsMap;
    }
}
