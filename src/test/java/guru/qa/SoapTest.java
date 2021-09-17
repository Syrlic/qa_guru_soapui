package guru.qa;

import io.restassured.RestAssured;
import io.restassured.internal.util.IOUtils;
import io.restassured.response.Response;
import io.spring.guides.gs_producing_web_service.Country;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoapTest {

    @Test
    void getCountry() throws IOException, JAXBException, SOAPException {
        JAXBContext jbCnxt = JAXBContext.newInstance(GetCountryResponse.class);
        Unmarshaller unmarshaller = jbCnxt.createUnmarshaller();

        InputStream is = SoapTest.class.getClassLoader().getResourceAsStream("getCountryRequest.xml");
        final String request = new String(IOUtils.toByteArray(is));

        RestAssured.baseURI = "http://localhost:8080/ws";

        Response response = given()
                .header("Content-Type", "text/xml")
                .and()
                .body(request)
                .when()
                .post("/getCountry")
                .then()
                .statusCode(200)
                .extract().response();

        assertTrue(response.asString().contains("EUR"));

        GetCountryResponse responseObj = (GetCountryResponse) unmarshaller.unmarshal(new StringReader(response.asString()));
        SOAPMessage message =
                MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(response.asByteArray()));
        JAXBContext jc = JAXBContext.newInstance(Country.class);
        Unmarshaller unmarshallerCountry = jc.createUnmarshaller();
        Country rc = (Country) unmarshaller.unmarshal(message.getSOAPBody().extractContentAsDocument());
        assertTrue(responseObj.getCountry().getName().equals("Spain"));
    }
}
