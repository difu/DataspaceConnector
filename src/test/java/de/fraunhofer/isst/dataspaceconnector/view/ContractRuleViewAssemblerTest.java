package de.fraunhofer.isst.dataspaceconnector.view;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;

import de.fraunhofer.isst.dataspaceconnector.controller.resources.RelationControllers;
import de.fraunhofer.isst.dataspaceconnector.controller.resources.ResourceControllers;
import de.fraunhofer.isst.dataspaceconnector.model.ContractRule;
import de.fraunhofer.isst.dataspaceconnector.model.ContractRuleDesc;
import de.fraunhofer.isst.dataspaceconnector.model.ContractRuleFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@SpringBootTest(classes = {ContractRuleViewAssembler.class, ViewAssemblerHelper.class,
        ContractRuleFactory.class})
public class ContractRuleViewAssemblerTest {

    @Autowired
    private ContractRuleViewAssembler contractRuleViewAssembler;

    @Autowired
    private ContractRuleFactory contractRuleFactory;

    @Test
    public void getSelfLink_inputNull_returnBasePathWithoutId() {
        /* ARRANGE */
        final var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .toUriString();
        final var path = ResourceControllers.RuleController.class
                .getAnnotation(RequestMapping.class).value()[0];
        final var rel = "self";

        /* ACT */
        final var result = contractRuleViewAssembler.getSelfLink(null);

        /* ASSERT */
        assertNotNull(result);
        assertEquals(baseUrl + path, result.getHref());
        assertEquals(rel, result.getRel().value());
    }

    @Test
    public void getSelfLink_validInput_returnSelfLink() {
        /* ARRANGE */
        final var contractRuleId = UUID.randomUUID();
        final var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .toUriString();
        final var path = ResourceControllers.RuleController.class
                .getAnnotation(RequestMapping.class).value()[0];
        final var rel = "self";

        /* ACT */
        final var result = contractRuleViewAssembler.getSelfLink(contractRuleId);

        /* ASSERT */
        assertNotNull(result);
        assertEquals(baseUrl + path + "/" + contractRuleId, result.getHref());
        assertEquals(rel, result.getRel().value());
    }

    @Test
    public void toModel_inputNull_throwIllegalArgumentException() {
        /* ACT && ASSERT */
        assertThrows(IllegalArgumentException.class,
                () -> contractRuleViewAssembler.toModel(null));
    }

    @Test
    public void toModel_validInput_returnContractRuleView() {
        /* ARRANGE */
        final var contractRule = getContractRule();

        /* ACT */
        final var result = contractRuleViewAssembler.toModel(contractRule);

        /* ASSERT */
        assertNotNull(result);
        assertEquals(contractRule.getTitle(), result.getTitle());
        assertEquals(contractRule.getValue(), result.getValue());
        assertEquals(contractRule.getCreationDate(), result.getCreationDate());
        assertEquals(contractRule.getModificationDate(), result.getModificationDate());
        assertEquals(contractRule.getAdditional(), result.getAdditional());

        final var selfLink = result.getLink("self");
        assertTrue(selfLink.isPresent());
        assertNotNull(selfLink.get());
        assertEquals(getContractRuleLink(contractRule.getId()), selfLink.get().getHref());

        final var contractsLink = result.getLink("contracts");
        assertTrue(contractsLink.isPresent());
        assertNotNull(contractsLink.get());
        assertEquals(getContractRuleContractsLink(contractRule.getId()),
                contractsLink.get().getHref());
    }

    /**************************************************************************
     * Utilities.
     *************************************************************************/

    private ContractRule getContractRule() {
        final var desc = new ContractRuleDesc();
        desc.setTitle("title");
        desc.setValue("value");
        final var contractRule = contractRuleFactory.create(desc);

        final var date = ZonedDateTime.now(ZoneOffset.UTC);
        final var additional = new HashMap<String, String>();
        additional.put("key", "value");

        ReflectionTestUtils.setField(contractRule, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(contractRule, "creationDate", date);
        ReflectionTestUtils.setField(contractRule, "modificationDate", date);
        ReflectionTestUtils.setField(contractRule, "additional", additional);

        return contractRule;
    }

    private String getContractRuleLink(final UUID contractRuleId) {
        final var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .toUriString();
        final var path = ResourceControllers.RuleController.class
                .getAnnotation(RequestMapping.class).value()[0];
        return baseUrl + path + "/" + contractRuleId;
    }

    private String getContractRuleContractsLink(final UUID contractRuleId) {
        return linkTo(methodOn(RelationControllers.RulesToContracts.class)
                .getResource(contractRuleId, null, null, null)).toString();
    }

}
