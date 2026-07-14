package com.example.attendance;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.attendance", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule entity_should_not_depend_on_other_layers =
        noClasses().that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().dependOnClassesThat().resideInAnyPackage(
                "..config..", "..common.."
            );

    @ArchTest
    static final ArchRule controller_should_not_access_repository_directly =
        noClasses().that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");
}
