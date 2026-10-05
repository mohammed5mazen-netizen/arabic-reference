package com.mrsoft.arabicreference.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.mrsoft.arabicreference", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleArchitectureTest {

    @ArchTest
    static final ArchRule domain_does_not_depend_on_outer_layers = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..api..",
                    "..infrastructure..",
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.servlet..");

    @ArchTest
    static final ArchRule api_does_not_depend_on_infrastructure = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule controllers_do_not_call_repositories = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.data.repository..",
                    "org.springframework.data.jpa..");

    @ArchTest
    static final ArchRule controllers_stay_out_of_domain = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..domain..");

    @ArchTest
    static final ArchRule modules_are_cycle_free = slices()
            .matching("com.mrsoft.arabicreference.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule search_port_is_a_domain_interface = classes()
            .that().haveSimpleName("LinguisticSearchPort")
            .should().beInterfaces()
            .andShould().resideInAPackage("com.mrsoft.arabicreference.search.domain");

    @ArchTest
    static final ArchRule no_search_engine_client_yet = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("org.elasticsearch..", "org.opensearch..");

    @ArchTest
    static final ArchRule no_public_account_types = classes()
            .that().haveSimpleName("User")
            .or().haveSimpleName("UserAccount")
            .or().haveSimpleName("JwtTokenService")
            .should().resideOutsideOfPackage("com.mrsoft.arabicreference..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule identity_has_no_implementation = classes()
            .that().resideInAPackage("com.mrsoft.arabicreference.identity")
            .should().haveSimpleName("package-info");

    @ArchTest
    static final ArchRule future_modules_have_no_implementation = classes()
            .that().resideInAnyPackage(
                    "com.mrsoft.arabicreference.content",
                    "com.mrsoft.arabicreference.learning",
                    "com.mrsoft.arabicreference.ai")
            .should().haveSimpleName("package-info");

    @ArchTest
    static final ArchRule dictionary_does_not_depend_on_ai = noClasses()
            .that().resideInAnyPackage(
                    "com.mrsoft.arabicreference.dictionary..",
                    "com.mrsoft.arabicreference.source..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.mrsoft.arabicreference.ai..",
                    "com.mrsoft.arabicreference.morphology..",
                    "org.elasticsearch..",
                    "org.opensearch..");

    @ArchTest
    static final ArchRule morphology_domain_does_not_depend_on_dictionary = noClasses()
            .that().resideInAPackage("com.mrsoft.arabicreference.morphology.domain..")
            .should().dependOnClassesThat().resideInAnyPackage("com.mrsoft.arabicreference.dictionary..");

    @ArchTest
    static final ArchRule grammar_domain_stays_inside_its_boundary = noClasses()
            .that().resideInAPackage("com.mrsoft.arabicreference.grammar.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.mrsoft.arabicreference.dictionary..",
                    "com.mrsoft.arabicreference.morphology..");

    @ArchTest
    static final ArchRule morphology_does_not_depend_on_grammar = noClasses()
            .that().resideInAPackage("com.mrsoft.arabicreference.morphology..")
            .should().dependOnClassesThat().resideInAPackage("com.mrsoft.arabicreference.grammar..");

    @ArchTest
    static final ArchRule grammar_root_stays_a_marker = classes()
            .that().resideInAPackage("com.mrsoft.arabicreference.grammar")
            .should().haveSimpleName("package-info");

    @ArchTest
    static final ArchRule morphology_root_stays_a_marker = classes()
            .that().resideInAPackage("com.mrsoft.arabicreference.morphology")
            .should().haveSimpleName("package-info");

    @ArchTest
    static final ArchRule search_domain_does_not_depend_on_other_infrastructure = noClasses()
            .that().resideInAPackage("com.mrsoft.arabicreference.search.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.mrsoft.arabicreference.dictionary.infrastructure..",
                    "com.mrsoft.arabicreference.grammar.infrastructure..",
                    "com.mrsoft.arabicreference.morphology.infrastructure..");

    @ArchTest
    static final ArchRule modules_do_not_depend_on_search_infrastructure = noClasses()
            .that().resideOutsideOfPackage("com.mrsoft.arabicreference.search..")
            .should().dependOnClassesThat().resideInAPackage("com.mrsoft.arabicreference.search.infrastructure..");
}
