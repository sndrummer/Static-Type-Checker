package edu.byu.yc.symboltable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Samuel Nuttall
 * <p>
 * Symbol Table, before creating a symbol table the qualified class visitor determines all of the
 * fully qualified class names and then a symbol table can be established
 */
public class SymbolTable implements ISymbolTable {

    private static Logger logger = LoggerFactory.getLogger(SymbolTable.class);

    private Map<String, ClassFieldsMethodsParams> classFieldsMethodsParamsMap = new HashMap<>(); //class name to fields, methods, and parameters
    private Map<String, String> classSimpleToQualifiedName;

    public SymbolTable(Map<String, String> classSimpleToQualifiedName) {
        this.classSimpleToQualifiedName = classSimpleToQualifiedName;

        for (Map.Entry<String, String> entry : classSimpleToQualifiedName.entrySet()) {
            classFieldsMethodsParamsMap.put(entry.getValue(), new ClassFieldsMethodsParams(entry.getValue()));
        }
    }

    @Override
    public String getFieldType(String classFQN, String fieldName) {
        ClassFieldsMethodsParams classFmp = classFieldsMethodsParamsMap.get(classFQN);
        return classFmp.getFieldTypeByName(fieldName);

    }

    @Override
    public String getMethodReturnType(String classFQN, String methodName) {
        ClassFieldsMethodsParams classFPM = classFieldsMethodsParamsMap.get(classFQN);
        return classFPM.getMethodTypeByName(methodName);
    }

    @Override
    public String getParameterType(String classFQN, String methodName, String paramName) {
        ClassFieldsMethodsParams classFPM = classFieldsMethodsParamsMap.get(classFQN);
        return classFPM.getParamTypeByName(methodName, paramName);
    }

    @Override
    public boolean classExists(String classFQN) {
        return classFieldsMethodsParamsMap.get(classFQN) != null;
    }

    @Override
    public boolean methodExists(String classFQN, String methodName) {
        ClassFieldsMethodsParams classFPM = classFieldsMethodsParamsMap.get(classFQN);
        if (classFPM == null) {
            return false;
        }

        Map<ASTNameType, List<ASTNameType>> methodMap = classFPM.getMethodParamsMap();
        for (Map.Entry<ASTNameType, List<ASTNameType>> entry : methodMap.entrySet()) {
            if (entry.getKey().getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean parameterExists(String classFQN, String methodName, String paramName) {

        ClassFieldsMethodsParams classFPM = classFieldsMethodsParamsMap.get(classFQN);
        if (classFPM == null) {
            return false;
        }

        Map<ASTNameType, List<ASTNameType>> methodMap = classFPM.getMethodParamsMap();
        for (Map.Entry<ASTNameType, List<ASTNameType>> entry : methodMap.entrySet()) {
            if (entry.getKey().getName().equals(methodName)) {
                for (ASTNameType nameType: entry.getValue()) {
                    if (nameType.getName().equals(paramName)) return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean fieldExists(String classFQN, String fieldName) {
        ClassFieldsMethodsParams classFPM = classFieldsMethodsParamsMap.get(classFQN);
        if (classFPM == null) {
            return false;
        }
        List<ASTNameType> fields = classFPM.getFields();
        for (ASTNameType field : fields) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ISymbolTable addLocal(String name, String type) {
        return null;
    }

    @Override
    public ISymbolTable removeLocal(String name) {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SymbolTable\n{\n");
        for (Map.Entry<String, ClassFieldsMethodsParams> entry : classFieldsMethodsParamsMap.entrySet()) {
            sb.append(entry.getValue().toString());
        }
        sb.append("\n}");
        return sb.toString();
    }

    public Map<String, ClassFieldsMethodsParams> getClassFieldsMethodsParamsMap() {
        return classFieldsMethodsParamsMap;
    }

    public void addNewClass(ClassFieldsMethodsParams classFieldsMethodsParams) {
        classFieldsMethodsParamsMap.put(classFieldsMethodsParams.getClassFQN(), classFieldsMethodsParams);
    }

    public void addField(String curClassName, ASTNameType field) {

        ClassFieldsMethodsParams classFMP = classFieldsMethodsParamsMap.get(curClassName);
        if (classFMP == null) {
            logger.error("Class not found");
            return;
        }
        classFMP.addField(field);
    }

    public void addMethod(String curClassName, ASTNameType method, List<ASTNameType> params) {
        ClassFieldsMethodsParams classFMP = classFieldsMethodsParamsMap.get(curClassName);
        classFMP.addMethod(method, params);
    }

    public Map<String, String> getClassSimpleToQualifiedName() {
        return classSimpleToQualifiedName;
    }

}
