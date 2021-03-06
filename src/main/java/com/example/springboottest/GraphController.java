package com.example.springboottest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/")
public class GraphController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("graphs")
	public List<Graph> graph(@RequestParam(value = "name", defaultValue = "gaffer") String name) {
		OpenShiftClient osClient = new DefaultOpenShiftClient();

		int randomNumber = ThreadLocalRandom.current().nextInt();

		String jobName = "job-" + randomNumber;

		Job aJob = new JobBuilder()
				.withNewMetadata().withName(jobName).addToLabels("job-name", jobName).endMetadata()
				.withNewSpec()
				.withNewTemplate()
				.withNewMetadata().addToLabels("job-name", jobName).endMetadata()
				.withNewSpec()
				.withRestartPolicy("Never")
				.addNewContainer().withName(jobName).withImage("registry.access.redhat.com/rhel7/rhel:latest")
				.withCommand("/bin/bash", "-c", "for i in {1..5}; do echo hi stuff; sleep 5; done")
				.withNewResources()
				.addToRequests("cpu", new Quantity("100m"))
				.addToRequests("memory", new Quantity("128Mi"))
				.addToLimits("cpu", new Quantity("100m"))
				.addToLimits("memory", new Quantity("128Mi"))
				.endResources()
				.endContainer()
				.endSpec()
				.endTemplate()
				.endSpec().build();

		osClient.batch().jobs().create(aJob);
		ArrayList<Graph> graphList = new ArrayList<>();
		graphList.add(new Graph("OurGraph", "YES"));
		return graphList;
	}

	@PostMapping("addGraph")
	public Graph graph(@RequestBody Graph graph) throws IOException {
		OpenShiftClient osClient = new DefaultOpenShiftClient();
		// Create Custom Resource Context
		CustomResourceDefinitionContext context = new CustomResourceDefinitionContext
				.Builder()
				.withGroup("gchq.gov.uk")
				.withKind("Gaffer")
				.withName("gaffers.gchq.gov.uk")
				.withPlural("gaffers")
				.withScope("Namespaced")
				.withVersion("v1")
				.build();

		// Load from Yaml
		Map<String, Object> dummyObject = osClient.customResource(context)
				.load(GraphController.class.getResourceAsStream("/add-gaffer.yaml"));
		// Create Custom Resource
		osClient.customResource(context).create("default", dummyObject);
		return new Graph("RoadTraffic", "Graph added");
	}

	@PostMapping("auth")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new JwtResponse(token));
	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}

	@DeleteMapping("deleteGraph/{id}")
	public String deleteGraph(@PathVariable Long id){
		return "Record Deleted " + id;
	}
}