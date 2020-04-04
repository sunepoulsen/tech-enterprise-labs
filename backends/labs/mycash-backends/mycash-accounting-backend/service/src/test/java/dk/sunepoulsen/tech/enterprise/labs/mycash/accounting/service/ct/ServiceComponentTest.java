package dk.sunepoulsen.tech.enterprise.labs.mycash.accounting.service.ct;

import dk.sunepoulsen.tech.enterprise.labs.core.rs.client.TechEnterpriseLabsClient;
import dk.sunepoulsen.tech.enterprise.labs.core.rs.client.exceptions.ClientBadRequestException;
import dk.sunepoulsen.tech.enterprise.labs.core.rs.client.exceptions.ClientConflictException;
import dk.sunepoulsen.tech.enterprise.labs.mycash.accounting.rs.client.AccountingIntegrator;
import dk.sunepoulsen.tech.enterprise.labs.mycash.accounting.rs.client.model.Accounting;
import dk.sunepoulsen.tech.enterprise.labs.mycash.accounting.service.Application;
import dk.sunepoulsen.tech.enterprise.labs.mycash.accounting.service.domain.accountings.AccountingTransformations;
import dk.sunepoulsen.tech.enterprise.labs.mycash.accounting.service.domain.persistence.AccountingEntity;
import dk.sunepoulsen.tech.enterprise.labs.mycash.accounting.service.domain.persistence.AccountingRepository;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceComponentTest {
    @LocalServerPort
    private int serverPort;

    @Autowired
    private AccountingRepository repository;

    private AccountingIntegrator integrator;

    @Before
    public void setupIntegrator() throws Exception {
        this.integrator = new AccountingIntegrator(new TechEnterpriseLabsClient(new URI("http://localhost:" + serverPort)));
    }

    @Test
    public void testCreateAccounting_Success() throws Exception {
        Accounting newAccounting = new Accounting();
        newAccounting.setName("name");
        newAccounting.setDescription("description");

        Accounting createdAccounting = this.integrator.createAccounting(newAccounting).blockingGet();
        assertThat(createdAccounting.getId(), notNullValue());

        newAccounting.setId(createdAccounting.getId());
        assertThat(newAccounting, equalTo(createdAccounting));

        Optional<AccountingEntity> optionalAccountingEntity = repository.findById(createdAccounting.getId());
        assertThat(optionalAccountingEntity.isEmpty(), equalTo(false));
        MatcherAssert.assertThat(newAccounting, equalTo(AccountingTransformations.fromEntity(optionalAccountingEntity.get())));
    }

    @Test(expected = ClientBadRequestException.class)
    public void testCreateAccounting_BadRequest() throws Exception {
        this.integrator.createAccounting(new Accounting()).blockingGet();
    }

    @Test(expected = ClientConflictException.class)
    public void testCreateAccounting_Conflict() throws Exception {
        Accounting newAccounting = new Accounting();
        newAccounting.setName("name");
        newAccounting.setDescription("description");

        this.integrator.createAccounting(newAccounting).blockingGet();
        this.integrator.createAccounting(newAccounting).blockingGet();
    }
}