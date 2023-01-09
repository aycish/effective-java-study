package me.jounhee.chapter01.item01.factory;

public class PersonFactory {
	public Person createPerson(String type) {
		Person returnPerson = null;
		switch (type) {
			case "A":
				returnPerson = new PersonA();
				break;
			case "B":
				returnPerson = new PersonB();
				break;
			case "C":
				returnPerson = new PersonC();
				break;
		}
		return returnPerson;
	}
}
