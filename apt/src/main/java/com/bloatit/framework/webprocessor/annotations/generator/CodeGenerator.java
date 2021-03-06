package com.bloatit.framework.webprocessor.annotations.generator;

import com.bloatit.framework.webprocessor.annotations.LengthConstraint;
import com.bloatit.framework.webprocessor.annotations.MaxConstraint;
import com.bloatit.framework.webprocessor.annotations.MinConstraint;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.PrecisionConstraint;
import com.bloatit.framework.webprocessor.annotations.generator.Generator.Attribute;
import com.bloatit.framework.webprocessor.annotations.generator.Generator.Clazz;
import com.bloatit.framework.webprocessor.annotations.generator.Generator.Method;
import com.bloatit.framework.webprocessor.annotations.generator.Generator.MethodCall;
import com.bloatit.framework.webprocessor.annotations.generator.Generator.Modifier;

public class CodeGenerator {

    public Clazz generateUrlClass(final UrlDescription desc) {
        final Clazz clazz = new Generator.Clazz(desc.getClassName(), "com.bloatit.web.url");
        clazz.addImport("com.bloatit.framework.webprocessor.annotations.ParamContainer.Protocol");
        clazz.addImport("com.bloatit.framework.utils.parameters.*");
        clazz.addImport("com.bloatit.framework.webprocessor.url.*");
        clazz.addImport("com.bloatit.framework.exceptions.highlevel.BadProgrammerException;");

        clazz.addImplements("Cloneable");
        if (desc.getFather() == null) {
            clazz.setExtends("Url");
        } else {
            clazz.setExtends(desc.getFather().getClassName());
        }

        final Attribute pageCodeStatic = clazz.addAttribute("String", "PAGE_CODE");
        pageCodeStatic.setStatic(true);
        pageCodeStatic.setStaticEquals(Utils.getStr(desc.getComponent().getCodeName().replaceAll("\\%[a-zA-Z0-9_#()\\.]+\\%", "[a-zA-Z0-9_%]+")));
        clazz.addAttribute(desc.getComponent().getClassName(), "component");

        final Method staticGetName = clazz.addMethod("boolean", "matches");
        staticGetName.setStaticFinal("static");
        staticGetName.addParameter("String", "pageCode");
        staticGetName.addLine("return pageCode.matches(PAGE_CODE);");

        final Method protectedConstructor = clazz.addConstructor();
        protectedConstructor.setModifier(Modifier.PROTECTED);
        protectedConstructor.addParameter("Parameters", "params");
        protectedConstructor.addParameter("SessionParameters", "session");
        if (desc.getFather() != null) {
            protectedConstructor.addLine("super(params, session);");
        }
        protectedConstructor.addLine("this.component = new " + desc.getComponent().getClassName() + "(params, session);");

        final Method constructor = clazz.addConstructor();
        constructor.addParameter("String", "pagename");
        constructor.addParameter("Parameters", "postGetParameters");
        constructor.addParameter("SessionParameters", "session");
        constructor.addLine("this(parseParameters(pagename, postGetParameters), session);");
        constructor.addLine("if (!matches(pagename)) {");
        constructor.addLine("    throw new BadProgrammerException(\"The pagename is not corresponding to this url.\");");
        constructor.addLine("}");

        final Method staticParser = clazz.addMethod("Parameters", "parseParameters");
        staticParser.setStaticFinal("static ");
        staticParser.addParameter("String", "pagename");
        staticParser.addParameter("Parameters", "postGetParameters");
        staticParser.addLine("// Add the parameters from the pagename");
        final String codeName = desc.getComponent().getCodeName();
        final String[] splited = codeName.split("/");
        if (codeName.contains("%")) {
            staticParser.addLine("final String[] split = pagename.split(\"/\");");
        }
        for (int i = 0; i < splited.length; i++) {
            if (splited[i].startsWith("%") && splited[i].endsWith("%")) {
                staticParser.addLine("postGetParameters.add(\"" + splited[i].replaceAll("\\#.*", "").replace("%", "") + "\", split[" + i + "]);");
            }
        }
        staticParser.addLine("return postGetParameters;");

        final Method copyConstructor = clazz.addConstructor();
        copyConstructor.setModifier(Modifier.PROTECTED);
        copyConstructor.addParameter(clazz.getName(), "other");
        copyConstructor.addLine("super(other);");
        copyConstructor.addLine("component = other.component.clone();");

        // Generated constructor
        {
            final Method generatedConstructor = clazz.addConstructor();
            final MethodCall superMethod = new MethodCall("super");
            for (final ParameterDescription paramDesc : desc.getFathersConstructorParameters()) {
                generatedConstructor.addParameter(paramDesc.getTypeOrTemplateType(), paramDesc.getAttributeName());
                superMethod.addParameter(paramDesc.getAttributeName());
                if (paramDesc.isRawType()) {
                    generatedConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            final MethodCall componentConstruction = new MethodCall(desc.getComponent().getClassName());
            for (final ParameterDescription paramDesc : desc.getConstructorParameters()) {
                generatedConstructor.addParameter(paramDesc.getTypeOrTemplateType(), paramDesc.getAttributeName());
                componentConstruction.addParameter(paramDesc.getAttributeName());
                if (paramDesc.isRawType()) {
                    generatedConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            componentConstruction.addParameter("false");

            generatedConstructor.addLine(superMethod + ";");
            generatedConstructor.addLine("component = new " + componentConstruction + ";");
        }

        // Generated safe
        {
            final Method generatedForceConstructor = clazz.addConstructor();
            final MethodCall superMethod = new MethodCall("super");
            for (final ParameterDescription paramDesc : desc.getFathersConstructorParameters()) {
                generatedForceConstructor.addParameter(paramDesc.getTypeOrTemplateType(), paramDesc.getAttributeName());
                superMethod.addParameter(paramDesc.getAttributeName());
                if (paramDesc.isRawType()) {
                    generatedForceConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }

            final MethodCall componentConstruction = new MethodCall(desc.getComponent().getClassName());
            for (final ParameterDescription paramDesc : desc.getConstructorParameters()) {
                generatedForceConstructor.addParameter(paramDesc.getTypeOrTemplateType(), paramDesc.getAttributeName());
                componentConstruction.addParameter(paramDesc.getAttributeName());
                if (paramDesc.isRawType()) {
                    generatedForceConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            componentConstruction.addParameter("force");

            generatedForceConstructor.addParameter("boolean", "force");
            generatedForceConstructor.addLine(superMethod + ";");
            generatedForceConstructor.addLine("component = new " + componentConstruction + ";");
        }

        // Is action
        if (desc.getFather() == null) {
            final Method isAction = clazz.addMethod("boolean", "isAction");
            isAction.setOverride();
            isAction.addLine("return " + (desc.isAction() ? "true;" : "false;"));
        }

        final Method getProtocol = clazz.addMethod("Protocol", "getProtocol");
        getProtocol.setOverride();
        getProtocol.addLine("return Protocol." + desc.getComponent().getProtocol() + ";");

        final Method getCode = clazz.addMethod("String", "getCode");
        getCode.setOverride();
        getCode.addLine("StringBuilder sb = new StringBuilder();");

        // final String codeName = desc.getComponent().getCodeName();
        // final String[] splited = codeName.split("%");
        for (int i = 0; i < splited.length; ++i) {
            final String paramInUrl = splited[i];
            if (paramInUrl.startsWith("%") && paramInUrl.endsWith("%")) {
                if (paramInUrl.contains("#")) {
                    getCode.addLine("sb.append(" + paramInUrl.replaceAll(".*\\#", "").replace("%", "") + ");");
                } else {
                    getCode.addLine("sb.append(get" + Utils.firstCharUpper(paramInUrl.replace("%", "")) + "Parameter().getStringValue());");
                }
            } else {
                getCode.addLine("sb.append(" + Utils.getStr(paramInUrl) + ");");
            }
            if (i < (splited.length - 1)) {
                getCode.addLine("sb.append(\"/\");");
            }
        }

        getCode.addLine("return sb.toString();");

        final Method doConstructUrl = clazz.addMethod("void", "doConstructUrl");
        doConstructUrl.setOverride();
        doConstructUrl.addParameter("StringBuilder", "sb");
        if (desc.getFather() != null) {
            doConstructUrl.addLine("super.doConstructUrl(sb);");
        }
        doConstructUrl.addLine("component.constructUrl(sb);");

        final Method doGetStringParameters = clazz.addMethod("void", "doGetParametersAsStrings");
        doGetStringParameters.setOverride();
        doGetStringParameters.addParameter("Parameters", "parameters");
        if (desc.getFather() != null) {
            doGetStringParameters.addLine("super.doGetParametersAsStrings(parameters);");
        }
        doGetStringParameters.addLine("component.getParametersAsStrings(parameters);");

        final Method addParameter = clazz.addMethod("void", "addParameter");
        addParameter.addAnnotation("@SuppressWarnings(\"deprecation\")");
        addParameter.setOverride();
        addParameter.addParameter("String", "key");
        addParameter.addParameter("String", "value");
        if (desc.getFather() != null) {
            addParameter.addLine("super.addParameter(key, value);");
        }
        addParameter.addLine("component.addParameter(key, value);");

        // @Override
        // public UrlParameter<?, ?> getParameter(String name) {
        // return null;
        // }
        Method getParameterMethod = clazz.addMethod("UrlParameter<?, ?>", "getParameter");
        getParameterMethod.addParameter("String", "name");
        getParameterMethod.setOverride();
        addNamedParameter(getParameterMethod, desc.getComponent(), "");
        addNameParameterComponents(desc.getComponent(), getParameterMethod, "");
        if (desc.getFather() != null) {
            getParameterMethod.addLine("return super.getParameter(name);");
        } else {
            getParameterMethod.addLine("return null;");
        }

        final Method getMessages = clazz.addMethod("Messages", "getMessages");
        getMessages.setOverride();
        if (desc.getFather() != null) {
            getMessages.addLine("final Messages messages = super.getMessages();");
            getMessages.addLine("messages.addAll(this.component.getMessages());");
            getMessages.addLine("return messages;");
        } else {
            getMessages.addLine("return this.component.getMessages();");
        }

        final Method clone = clazz.addMethod(clazz.getName(), "clone");
        clone.setOverride();
        clone.addLine("return new " + clazz.getName() + "(this);");

        for (final ParameterDescription param : desc.getComponent().getParameters()) {
            final String getterName = "get" + Utils.firstCharUpper(param.getAttributeName());
            final Method getter = clazz.addMethod(param.getTypeWithoutTemplate(), getterName);
            getter.addLine("return this.component." + getterName + "();");

            final String getParameterName = "get" + Utils.firstCharUpper(param.getAttributeName()) + "Parameter";
            final String template = "<" + param.getTypeWithoutTemplate() + ", " + param.getTypeOrTemplateType() + ">";
            final Method getParameter = clazz.addMethod("UrlParameter" + template, getParameterName);
            getParameter.addLine("return this.component." + getParameterName + "();");

            final String setterName = "set" + Utils.firstCharUpper(param.getAttributeName());
            final Method setter = clazz.addMethod("void", setterName);
            setter.addParameter(param.getTypeWithoutTemplate(), "other");
            setter.addLine("this.component." + setterName + "(other, false);");
            if (param.isRawType()) {
                getter.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                getParameter.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                setter.addAnnotation("@SuppressWarnings(\"rawtypes\")");
            }
        }

        for (final ComponentDescription subComponent : desc.getComponent().getSubComponents()) {
            final String getParameterName = "get" + Utils.firstCharUpper(subComponent.getAttributeName()) + "Url";
            final Method getParameter = clazz.addMethod(subComponent.getClassName(), getParameterName);
            getParameter.addLine("return this.component." + getParameterName + "();");

            final String setterName = "set" + Utils.firstCharUpper(subComponent.getAttributeName()) + "Url";
            final Method setter = clazz.addMethod("void", setterName);
            setter.addParameter(subComponent.getClassName(), "other");
        }

        return clazz;
    }

    private void addNameParameterComponents(final ComponentDescription componentDescription, Method getParameterMethod, String cmpName) {
        for (ComponentDescription scomponents : componentDescription.getSubComponents()) {
            addNamedParameter(getParameterMethod, scomponents, cmpName + "get" + Utils.firstCharUpper(scomponents.getAttributeName()) + "Url().");
            addNameParameterComponents(scomponents, getParameterMethod, cmpName + "get" + Utils.firstCharUpper(scomponents.getAttributeName())
                    + "Url().");
        }
    }

    private void addNamedParameter(Method getParameterMethod, ComponentDescription scomponents, String cmpName) {
        for (ParameterDescription param : scomponents.getParameters()) {
            getParameterMethod.addLine("if (" + param.getNameStr() + ".equals(name)){");
            getParameterMethod.addLine("    return " + cmpName + "get" + Utils.firstCharUpper(param.getAttributeName()) + "Parameter();");
            getParameterMethod.addLine("}");
        }
    }

    public Clazz generateComponentClass(final ComponentDescription desc) {
        final Clazz clazz = new Generator.Clazz(desc.getClassName(), "com.bloatit.web.url");
        clazz.setExtends("UrlComponent");
        clazz.addImport("com.bloatit.framework.utils.parameters.*");
        clazz.addImport("com.bloatit.framework.webprocessor.url.*");

        final Method constructor = clazz.addConstructor();

        constructor.addParameter("Parameters", "params");
        constructor.addParameter("SessionParameters", "session");

        final Method generatedConstructor = clazz.addConstructor();
        Method defaultConstructor = null;
        if (!desc.getUrlParameters().isEmpty()) {
            generatedConstructor.addLine("this();");
            constructor.addLine("this();");
            defaultConstructor = clazz.addConstructor();
            defaultConstructor.setModifier(Modifier.PRIVATE);
        } else {
            defaultConstructor = generatedConstructor;
            constructor.addLine("this(false);");
        }
        boolean hasConstraint = false;
        for (final ParameterDescription param : desc.getParameters()) {

            final NonOptional nonOptional = param.getNonOptional();
            if (nonOptional != null) {
                final MethodCall call = new MethodCall("OptionalConstraint<" + param.getTypeWithoutTemplate() + ">");
                call.addParameter(Utils.getStr(nonOptional.value().value()));
                defaultConstructor.addLine(param.getAttributeName() + ".addConstraint(new " + call.toString() + ");");
                hasConstraint = true;
                if (param.isRawType()) {
                    defaultConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            final LengthConstraint lengthConstraint = param.getLengthConstraint();
            if (lengthConstraint != null) {
                final MethodCall call = new MethodCall("LengthConstraint<" + param.getTypeWithoutTemplate() + ">");
                call.addParameter(Utils.getStr(lengthConstraint.message().value()));
                call.addParameter(String.valueOf(lengthConstraint.length()));
                defaultConstructor.addLine(param.getAttributeName() + ".addConstraint(new " + call.toString() + ");");
                hasConstraint = true;
                if (param.isRawType()) {
                    defaultConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            final MaxConstraint maxConstraint = param.getMaxConstraint();
            if (maxConstraint != null) {
                final MethodCall call = new MethodCall("MaxConstraint<" + param.getTypeWithoutTemplate() + ">");
                call.addParameter(Utils.getStr(maxConstraint.message().value()));
                call.addParameter(String.valueOf(maxConstraint.max()));
                call.addParameter(String.valueOf(maxConstraint.isExclusive()));
                defaultConstructor.addLine(param.getAttributeName() + ".addConstraint(new " + call.toString() + ");");
                hasConstraint = true;
                if (param.isRawType()) {
                    defaultConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            final MinConstraint minConstraint = param.getMinConstraint();
            if (minConstraint != null) {
                final MethodCall call = new MethodCall("MinConstraint<" + param.getTypeWithoutTemplate() + ">");
                call.addParameter(Utils.getStr(minConstraint.message().value()));
                call.addParameter(String.valueOf(minConstraint.min()));
                call.addParameter(String.valueOf(minConstraint.isExclusive()));
                defaultConstructor.addLine(param.getAttributeName() + ".addConstraint(new " + call.toString() + ");");
                hasConstraint = true;
                if (param.isRawType()) {
                    defaultConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            final PrecisionConstraint precisionConstraint = param.getPrecisionConstraint();
            if (precisionConstraint != null) {
                final MethodCall call = new MethodCall("PrecisionConstraint<" + param.getTypeWithoutTemplate() + ">");
                call.addParameter(Utils.getStr(precisionConstraint.message().value()));
                call.addParameter(String.valueOf(precisionConstraint.precision()));
                defaultConstructor.addLine(param.getAttributeName() + ".addConstraint(new " + call.toString() + ");");
                hasConstraint = true;
                if (param.isRawType()) {
                    defaultConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
        }
        if (hasConstraint) {
            clazz.addImport("com.bloatit.framework.webprocessor.url.constraints.*");
        }

        for (final ParameterDescription param : desc.getUrlParameters()) {
            if (param.isRawType()) {
                generatedConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
            }
            generatedConstructor.addParameter(param.getTypeWithoutTemplate(), param.getAttributeName());
            generatedConstructor.addLine("this.set" + Utils.firstCharUpper(param.getAttributeName()) + "(" + param.getAttributeName() + ", force);");
        }

        final Method doRegister = clazz.addMethod("void", "doRegister");
        doRegister.setOverride();

        final Method clone = clazz.addMethod(clazz.getName(), "clone");
        clone.setOverride();
        clone.addLine(clazz.getName() + " other = new " + clazz.getName() + "((Parameters) null, (SessionParameters) null);");

        for (final ParameterDescription param : desc.getParameters()) {
            final String template = "<" + param.getTypeWithoutTemplate() + ", " + param.getTypeOrTemplateType() + ">";

            // Attribute
            final Attribute attribute = clazz.addAttribute("UrlParameter" + template, param.getAttributeName());

            // Getter
            final Method getter = clazz.addMethod("UrlParameter" + template, "get" + Utils.firstCharUpper(param.getAttributeName()) + "Parameter");
            getter.addLine("return this." + param.getAttributeName() + ";");

            // Static equals ( = new UrlParameter ...)
            final MethodCall newParamDescription = new Generator.MethodCall("UrlParameterDescription<" + param.getTypeOrTemplateType() + ">");
            newParamDescription.addParameter(param.getNameStr());
            newParamDescription.addParameter(param.getTypeOrTemplateType() + ".class");
            newParamDescription.addParameter(param.getRole());
            newParamDescription.addParameter(param.getDefaultValueStr());
            newParamDescription.addParameter(param.getSuggestedValueStr());
            newParamDescription.addParameter(param.getConversionErrorMsgStr());
            newParamDescription.addParameter(param.isOptional() ? "true" : "false");
            final MethodCall newParam = new Generator.MethodCall("UrlParameter" + template);
            if (param.getTypeOrTemplateType().equals(param.getTypeWithoutTemplate())) {
                newParam.addParameter("null");
            } else {
                clazz.addImport("java.util.ArrayList");
                newParam.addParameter("new ArrayList<" + param.getTypeOrTemplateType() + ">()");
            }
            newParam.addParameter("new " + newParamDescription);
            attribute.setStaticEquals("new " + newParam);

            // Value getter
            final Method valueGetter = clazz.addMethod(param.getTypeWithoutTemplate(), "get" + Utils.firstCharUpper(param.getAttributeName()));
            valueGetter.addLine("return this." + param.getAttributeName() + ".getValue();");

            // Value Setter
            final Method setter = clazz.addMethod("void", "set" + Utils.firstCharUpper(param.getAttributeName()));
            setter.addParameter(param.getTypeWithoutTemplate(), "other");
            setter.addParameter("boolean", "force");
            setter.addLine("this." + param.getAttributeName() + ".setValue(other, force);");
            for (final ParameterDescription generatedParam : desc.getParameterGeneratedFromMe(param)) {
                setter.addLine("try {");
                setter.addLine("    " + generatedParam.getAttributeName() + ".setValue(" + param.getAttributeName() + ".getValue().get"
                        + Utils.firstCharUpper(generatedParam.getAttributeName()) + "(), force);");
                setter.addLine("} catch (final Exception e) {");
                setter.addLine("    Log.framework().warn(\"Error in pretty value generation.\", e);");
                setter.addLine("}");
            }
            if (desc.getParameterGeneratedFromMe(param).size() > 0) {
                clazz.addImport("com.bloatit.common.Log");
            }

            final Method setter2 = clazz.addMethod("void", "set" + Utils.firstCharUpper(param.getAttributeName()));
            setter2.addParameter(param.getTypeWithoutTemplate(), "other");
            setter2.addLine("this." + param.getAttributeName() + ".setValue(other, false);");

            doRegister.addLine("register(" + param.getAttributeName() + ");");

            clone.addLine("other." + param.getAttributeName() + " = this." + param.getAttributeName() + ".clone();");
            if (param.isRawType()) {
                attribute.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                getter.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                // newParamDescription.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                valueGetter.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                setter.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                setter2.addAnnotation("@SuppressWarnings(\"rawtypes\")");
            }
        }

        for (final ComponentDescription subComponent : desc.getSubComponents()) {
            final String subComponentName = Utils.firstCharLower(subComponent.getClassName());

            // Add an attribute
            clazz.addAttribute(subComponent.getClassName(), subComponentName, //
                               "get" + Utils.firstCharUpper(subComponent.getAttributeName()) + "Url", //
                               "set" + Utils.firstCharUpper(subComponent.getAttributeName()) + "Url");

            // register it
            doRegister.addLine("register(" + subComponentName + ");");

            // add it to the clone method
            clone.addLine("other." + subComponentName + " = this." + subComponentName + ".clone();");

            // Construct it
            constructor.addLine("this." + subComponentName + " = new " + subComponent.getClassName() + "(params, session);");

            final MethodCall subcomponentConstruction = new MethodCall(subComponent.getClassName());
            for (final ParameterDescription subParameters : subComponent.getAllUrlParameters()) {
                final String parameterName = subParameters.getAttributeName() + subComponent.getClassName();
                generatedConstructor.addParameter(subParameters.getTypeWithoutTemplate(), parameterName);
                subcomponentConstruction.addParameter(parameterName);
                if (subParameters.isRawType()) {
                    generatedConstructor.addAnnotation("@SuppressWarnings(\"rawtypes\")");
                }
            }
            subcomponentConstruction.addParameter("force");
            generatedConstructor.addLine("this." + subComponentName + " = new " + subcomponentConstruction + ";");

        }
        generatedConstructor.addParameter("boolean", "force");

        constructor.addLine("parseSessionParameters(session);");
        constructor.addLine("parseParameters(params);");

        clone.addLine("return other;");

        return clazz;
    }

}
