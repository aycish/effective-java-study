package me.jounhee.chapter01.item01.factory;

public class ClassB {
	public Person createPerson(String type) {
		PersonFactory factory = new PersonFactory();
		Person returnPerson = factory.createPerson(type);
		return returnPerson;
	}
}
