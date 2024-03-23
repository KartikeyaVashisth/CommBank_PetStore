package petstore.tests;

import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import petstore.constants.APIHttpStatus;
import petstore.pojo.PetstorePojo;
import petstore.pojo.PetstorePojo.Category;
import petstore.pojo.PetstorePojo.Tag;
import petstore.utils.Util;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PetstoreTestCases {
	
	Util util = new Util();

	@BeforeMethod
	public void allureReportSetup() {

		RestAssured.filters(new AllureRestAssured());
	}

	@Parameters({"baseURI"})
	@Test
	public void addNewPetTest(String baseURI) {

		Category category = new Category(1, "Dog");

		List<String> photourls = Arrays.asList("https://www.dog.com", "https://www.dogworld.com");

		Tag tag1 = new Tag(10, "red");
		Tag tag2 = new Tag(20, "black");
		List<Tag> tags = Arrays.asList(tag1, tag2);

		PetstorePojo pet = new PetstorePojo(util.generateRandomId(), category, "Doggie", photourls, tags, "available"); //1st Object

		Response response = RestAssured.given().log().all()
				.baseUri(baseURI)
				.contentType(ContentType.JSON)
				.body(pet) //serialization: POJO(Java Object) to Json.
				.when()
				.post("/pet");

		response.prettyPrint();
		Assert.assertEquals(response.statusCode(), APIHttpStatus.OK_200.getCode());

		//De-serialization:
		ObjectMapper mapper = new ObjectMapper();
		try {
			PetstorePojo petRes = mapper.readValue(response.getBody().asString(), PetstorePojo.class);

			Assert.assertEquals(petRes.getId(), pet.getId());
			Assert.assertEquals(petRes.getName(), pet.getName());
			Assert.assertEquals(petRes.getStatus(), "available");

			Assert.assertEquals(petRes.getCategory().getId(), pet.getCategory().getId());
			Assert.assertEquals(petRes.getCategory().getName(), pet.getCategory().getName());

			Assert.assertEquals(petRes.getPhotoUrls(), pet.getPhotoUrls());

			Assert.assertEquals(petRes.getTags().get(0).getId(), pet.getTags().get(0).getId());
			Assert.assertEquals(petRes.getTags().get(0).getName(), pet.getTags().get(0).getName());

			Assert.assertEquals(petRes.getTags().get(1).getId(), pet.getTags().get(1).getId());
			Assert.assertEquals(petRes.getTags().get(1).getName(), pet.getTags().get(1).getName());

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

	}

	@Parameters({"baseURI"})
	@Test
	public void findPetByStatus(String baseURI) {

		RestAssured.given().log().all()
		.baseUri(baseURI)
		.queryParam("status", "available,pending,sold")
		.when()
		.get("/pet/findByStatus")
		.then()
		.assertThat()
		.statusCode(200);
	}

	@Parameters({"baseURI"})
	@Test
	public void findPetByIdTest(String baseURI) {

		Category category = new Category(2, "Dog");

		List<String> photourls = Arrays.asList("https://www.doggie.com", "https://www.dogworlds.com");

		Tag tag1 = new Tag(30, "White");
		Tag tag2 = new Tag(40, "Blue");
		List<Tag> tags = Arrays.asList(tag1, tag2);

		PetstorePojo pet = new PetstorePojo(util.generateRandomId(), category, "Doggie", photourls, tags, "available");

		Response response = RestAssured.given().log().all()
				.baseUri(baseURI)
				.contentType(ContentType.JSON)
				.body(pet)
				.when()
				.post("/pet");

		Integer petId = response.jsonPath().get("id");
		System.out.println("pet id is : " + petId);

		//GET API to get the same pet:

		//2. get the same petId and verifying it: GET
		Response getResponse =	RestAssured.given()
				.baseUri(baseURI)
				.when().log().all()
				.get("/pet/"+ petId);

		getResponse.then().assertThat().statusCode(200);

		ObjectMapper mapper = new ObjectMapper();
		try {
			PetstorePojo petResponse = mapper.readValue(getResponse.body().asString(), PetstorePojo.class);

			System.out.println(petResponse.getId() + ":" + petResponse.getName() + ":" + petResponse.getStatus());

			Assert.assertEquals(petId, petResponse.getId());
			Assert.assertEquals(pet.getName(), petResponse.getName());
			Assert.assertEquals(pet.getStatus(), petResponse.getStatus());

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		Response get404Response = RestAssured.given().log().all()
				.baseUri(baseURI)
				.when()
				.get("/pet/"+ System.currentTimeMillis());

		get404Response.then().assertThat().statusCode(404);

	}

	@Parameters({"baseURI"})
	@Test
	public void updateExistingPet(String baseURI) {

		Category category = new Category(4, "Dog");

		List<String> photourls = Arrays.asList("https://www.dog.com", "https://www.dogworld.com");

		Tag tag1 = new Tag(30, "Brown");
		Tag tag2 = new Tag(40, "White");
		List<Tag> tags = Arrays.asList(tag1, tag2);

		PetstorePojo pet = new PetstorePojo(util.generateRandomId(), category, "Doggie", photourls, tags, "available"); 

		Response response = RestAssured.given().log().all()
				.baseUri(baseURI)
				.contentType(ContentType.JSON)
				.body(pet) 
				.when()
				.post("/pet");

		Integer petId = response.jsonPath().get("id");
		System.out.println("pet id is : " + petId);

		//update the existing pet:
		pet.setId(util.generateRandomId());
		pet.setName("German Shepherd");
		pet.setStatus("sold");

		//2. PUT - Update Pet
		RestAssured.given().log().all()
		.baseUri(baseURI)
		.contentType(ContentType.JSON)
		.body(pet)
		.when().log().all()
		.put("/pet")
		.then().log().all()
		.assertThat()
		.statusCode(200)
		.and()
		.assertThat()
		.body("id", equalTo(pet.getId()))
		.and()
		.body("name", equalTo(pet.getName()))
		.and()
		.body("status", equalTo(pet.getStatus()));

	}

	@Parameters({"baseURI"})
	@Test
	public void updatePetWithFormData(String baseURI) {

		Category category = new Category(5, "Dog");

		List<String> photourls = Arrays.asList("https://www.dog.com", "https://www.dogworld.com");

		Tag tag1 = new Tag(50, "Brownie");
		Tag tag2 = new Tag(60, "Whitey");
		List<Tag> tags = Arrays.asList(tag1, tag2);
		
		PetstorePojo pet = new PetstorePojo(util.generateRandomId(), category, "Doggie", photourls, tags, "available"); 

		Response response = RestAssured.given().log().all()
				.baseUri(baseURI)
				.contentType(ContentType.JSON)
				.body(pet) 
				.when()
				.post("/pet");

		Integer petId = response.jsonPath().get("id");
		System.out.println("pet id is : " + petId);

		RestAssured.given().log().all()
		.baseUri(baseURI)
		.formParam("name", "Pug")
		.formParam("status", "sold")
		.when()
		.post("/pet/" + petId)
		.then().log().all()
		.assertThat()
		.statusCode(200);

	}

	@Parameters({"baseURI"})
	@Test
	public void deletePet(String baseURI) {

		Category category = new Category(8, "Dog");

		List<String> photourls = Arrays.asList("https://www.dog.com", "https://www.dogworld.com");

		Tag tag1 = new Tag(50, "Brownie");
		Tag tag2 = new Tag(60, "Whitey");
		List<Tag> tags = Arrays.asList(tag1, tag2);
		
		PetstorePojo pet = new PetstorePojo(util.generateRandomId(), category, "Doggie", photourls, tags, "available"); 

		Response response = RestAssured.given().log().all()
				.baseUri(baseURI)
				.contentType(ContentType.JSON)
				.body(pet) 
				.when()
				.post("/pet");

		Integer petId = response.jsonPath().get("id");
		System.out.println("pet id is : " + petId);

		RestAssured.given().log().all()
		.baseUri(baseURI)
		.when()
		.delete("/pet/" + petId)
		.then().log().all()
		.assertThat()
		.statusCode(200);
		
		RestAssured.given().log().all()
		.baseUri(baseURI)
		.when()
		.delete("/pet/" + petId)
		.then().log().all()
		.assertThat()
		.statusCode(404);
	}
	
	@Parameters({"baseURI"})
	@Test
	public void uploadImageTest(String baseURI) {

		Category category = new Category(1, "Dog");

		List<String> photourls = Arrays.asList("https://www.dog.com", "https://www.dogworld.com");

		Tag tag1 = new Tag(70, "Brownie");
		Tag tag2 = new Tag(80, "Whitey");
		List<Tag> tags = Arrays.asList(tag1, tag2);

		PetstorePojo pet = new PetstorePojo(util.generateRandomId(), category, "Doggie", photourls, tags, "available"); 

		Response response = RestAssured.given().log().all()
				.baseUri(baseURI)
				.contentType(ContentType.JSON)
				.body(pet)
				.when()
				.post("/pet");

		Integer petId = response.jsonPath().get("id");
		System.out.println("pet id is : " + petId);

		RestAssured.given().log().all()
		.accept("application/json")
		.contentType(ContentType.MULTIPART)
		.multiPart("fileName", new File("./petImage.jpg"))
		.multiPart("additionalMetadata", "test")
		.when().log().all()
		.post("/pet/"+petId+"/uploadImage")
		.then().log().all()
		.assertThat()
		.statusCode(200);

	}
}
