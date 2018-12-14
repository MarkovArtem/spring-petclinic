/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.visit;

import java.util.Collection;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class VisitController {

	private static final String VIEWS_VISIT_CREATE_OR_UPDATE_FORM = "pets/createOrUpdateVisitForm";
	private final VisitRepository visits;
	private final VetRepository vets;
	private final PetRepository pets;

	public VisitController(VisitRepository visits, VetRepository vets, PetRepository pets) {
		this.visits = visits;
		this.vets = vets;
		this.pets = pets;
	}

	@Autowired
	@ModelAttribute("vets")
	public Collection<Vet> veterinariansList() {
		return this.vets.findAll();
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/owners/*/pets/{petId}/visits/new")
	public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		model.put("pet", pet);
		Visit visit = new Visit();
		model.put("visit", visit);
		pet.addVisit(visit);
		return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
		} else {
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/edit/{visitId}")
	public String initUpdateVisionForm(@PathVariable("visitId") int visitId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		model.put("pet", pet);
		Visit visit = this.visits.findById(visitId);
		model.put("visit", visit);
		pet.addVisit(visit);
		return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/edit/{visitId}")
	public String processUpdateVisionForm(@Valid Visit visit, BindingResult result,
			@PathVariable("visitId") int visitId) {
		if (result.hasErrors()) {
			return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
		} else {
			visit.setId(visitId);
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	/**
	 * Custom handler for updating visit cancel.
	 *
	 * @param petId   the ID of the pet
	 * @param ownerId the ID of the owner
	 * @param visitId the ID of the visit
	 * @return owner information page
	 */
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/edit/{visitId}/cancel")
	public String showCanceled(@PathVariable("petId") int petId, @PathVariable("ownerId") int ownerId,
			@PathVariable("visitId") int visitId) {
		Pet pet = this.pets.findById(petId);
		Visit visit = this.visits.findById(visitId);
		visit.setCanceled(true);
		visit.setId(visitId);
		pet.addVisit(visit);
		this.visits.save(visit);
		return "redirect:/owners/{ownerId}";
	}

}
