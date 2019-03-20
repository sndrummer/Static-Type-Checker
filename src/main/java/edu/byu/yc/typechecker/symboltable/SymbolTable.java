package edu.byu.yc.typechecker.symboltable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Samuel Nuttall
 * <p>
 * Symbol Table, before creating a symbol table the qualified class visitor determines all of the
 * fully qualified class names and then a symbol table can be established
 */
public class SymbolTable implements ISymbolTable {

    private static Logger logger = LoggerFactory.getLogger(SymbolTable.class);

    private Map<String, ClassProperties> classFieldsMethodsParamsMap = new HashMap<>(); //class name to fields, methods, and parameters
    private Map<String, String> classSimpleToQualifiedName;
    private Map<String, Set<String>> validTypes = new HashMap<>();

    private ASTClassValidator validator;

    public SymbolTable(Map<String, String> classSimpleToQualifiedName, ASTClassValidator classValidator) {
        this.classSimpleToQualifiedName = classSimpleToQualifiedName;

        for (Map.Entry<String, String> entry : classSimpleToQualifiedName.entrySet()) {
            classFieldsMethodsParamsMap.put(entry.getValue(), new ClassProperties(entry.getValue()));
        }

        this.validator = classValidator;
    }

    @Override
    public String getFieldType(String classFQN, String fieldName) {
        ClassProperties classFmp = classFieldsMethodsParamsMap.get(classFQN);
        return classFmp.getFieldTypeByName(fieldName);

    }

    @Override
    public String getMethodReturnType(String classFQN, String methodName) {
        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
        return classFPM.getMethodTypeByName(methodName);
    }

    @Override
    public String getParameterType(String classFQN, String methodName, String paramName) {
        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
        return classFPM.getParamTypeByName(methodName, paramName);
    }

    @Override
    public String getLocalVariableType(String classFQN, String methodName, String localVariableName) {
        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
        return classFPM.getLocalVariableTypeByName(methodName, localVariableName);
    }

    @Override
    public boolean classExists(String classFQN) {
        return classFieldsMethodsParamsMap.get(classFQN) != null;
    }

    @Override
    public boolean methodExists(String classFQN, String methodName) {
        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
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

        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
        if (classFPM == null) {
            return false;
        }
        return findMethodProperty(classFQN, methodName, paramName, classFPM.getMethodParamsMap());
    }

    @Override
    public boolean fieldExists(String classFQN, String fieldName) {
        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
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
    public boolean localVariableExists(String classFQN, String methodName, String localVariableName) {

        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
        if (classFPM == null) {
            return false;
        }
        return findMethodProperty(classFQN, methodName, localVariableName, classFPM.getLocalVariablesMap());

    }

    public boolean validTypeExists(String classFQN, String type) {
        if (getValidTypes().get(classFQN) == null) {
            return false;
        } else return getValidTypes().get(classFQN).contains(type);

    }

    private boolean findMethodProperty(String classFQN, String methodName, String propName,
                                       Map<ASTNameType, List<ASTNameType>> propMap) {
        ClassProperties classFPM = classFieldsMethodsParamsMap.get(classFQN);
        if (classFPM == null) {
            return false;
        }

        for (Map.Entry<ASTNameType, List<ASTNameType>> entry : propMap.entrySet()) {
            if (entry.getKey().getName().equals(methodName)) {
                for (ASTNameType nameType : entry.getValue()) {
                    if (nameType.getName().equals(propName)) return true;
                }
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
        for (Map.Entry<String, ClassProperties> entry : classFieldsMethodsParamsMap.entrySet()) {
            sb.append(entry.getValue().toString());
        }
        sb.append("\n\tValidTypes: ");
        sb.append(getValidTypes());
        sb.append("\n}");
        return sb.toString();
    }

    public Map<String, ClassProperties> getClassFieldsMethodsParamsMap() {
        return classFieldsMethodsParamsMap;
    }

    public void addNewClass(ClassProperties classProperties) {
        classFieldsMethodsParamsMap.put(classProperties.getClassFQN(), classProperties);
    }

    public void addField(String curClassName, ASTNameType field) {

        ClassProperties classFMP = classFieldsMethodsParamsMap.get(curClassName);
        if (classFMP == null) {
            logger.error("Class not found");
            return;
        }
        classFMP.addField(field);
    }

    public void addMethod(String curClassName, ASTNameType method, List<ASTNameType> params,
                          List<ASTNameType> localVariables) {
        ClassProperties classFMP = classFieldsMethodsParamsMap.get(curClassName);
        classFMP.addMethod(method, params, localVariables);
    }

    public Map<String, String> getClassSimpleToQualifiedName() {
        return classSimpleToQualifiedName;
    }

    public void addValidType(String curClassName, String type) {
        Set<String> classTypes;
        if (validTypes.get(curClassName) == null) {
            classTypes = new HashSet<>();
        } else classTypes = validTypes.get(curClassName);

        classTypes.add(type);

        validTypes.put(curClassName, classTypes);
    }

    public Map<String, Set<String>> getValidTypes() {
        return validTypes;
    }


    public ASTClassValidator getValidator() {
        return validator;
    }
}
