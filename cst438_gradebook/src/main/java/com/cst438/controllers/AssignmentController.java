package com.cst438.controllers;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;


@RestController
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
public class AssignmentController {

	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
	// get all assignments for an instructor for a course
	// As an instructor for a course , I can see all assignments in the course
		@GetMapping("/assignment")
		public AssignmentListDTO getAssignmentsFromCourse(@RequestParam("course_id") Integer course_id ) {
			
			List<Assignment> assignments = assignmentRepository.findAssignmentByCourseId(course_id);
			AssignmentListDTO result = new AssignmentListDTO();
			for (Assignment a: assignments) {
				result.assignments.add(new AssignmentListDTO.AssignmentDTO(a.getId(), a.getCourse().getCourse_id(), a.getName(), a.getDueDate().toString() , a.getCourse().getTitle()));
			}
			return result;
		}
		
		private Assignment checkAssignment(int assignmentId, String email) {
			// get assignment 
			Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
			if (assignment == null) {
				throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Assignment not found. "+assignmentId );
			}
			// check that user is the course instructor
			if (!assignment.getCourse().getInstructor().equals(email)) {
				throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
			}
			
			return assignment;
		}
		
//		@PostMapping("/assignment")
//		@Transactional
//		public AssignmentDTO newAssignment(@RequestBody AssignmentDTO dto) {
//			String userEmail = "dwisneski@csumb.edu";
//			// validate course and that the course instructor is the user
//			Course c = courseRepository.findById(dto.courseId).orElse(null);
//			if (c != null && c.getInstructor().equals(userEmail)) {
//				// create and save new assignment
//				// update and return dto with new assignment primary key
//				Assignment a = new Assignment();
//				a.setCourse(c);
//				a.setName(dto.name);
//				a.setDueDate(Date.valueOf(dto.dueDate));
//				a.setNeedsGrading(1);
//				a = assignmentRepository.save(a);
//				dto.id=a.getId();
//				return dto;
//				
//			} else {
//				// invalid course
//				throw new ResponseStatusException( 
//	                           HttpStatus.BAD_REQUEST, 
//	                          "Invalid course id.");
//			}
//		}

		
		@PostMapping("/assignment")
		@Transactional
		public AssignmentListDTO.AssignmentDTO addAssignment(@RequestBody AssignmentListDTO.AssignmentDTO a) { 
			
			String userEmail = "dwisneski@csumb.edu";
			
			// get course for assignment
			Course c = courseRepository.findById(a.courseId).orElse(null);
			
			if (c != null && c.getInstructor().equals(userEmail)) {
				Assignment assignment = new Assignment();
				assignment.setName(a.assignmentName);
				assignment.setCourse(c);
				Date date = java.sql.Date.valueOf(a.dueDate);
				assignment.setDueDate(date);
				assignment.setNeedsGrading(1);
				
				//Assignment savedAssignment = assignmentRepository.save(assignment);
				//AssignmentListDTO.AssignmentDTO result = createAssignmentDTO(savedAssignment);
				
				assignment = assignmentRepository.save(assignment);
				a.assignmentId = assignment.getId();
				
				//System.out.printf("%s\n", assignment.getName());
				
				return a;
			
			} else {
				// invalid course
				throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Invalid course id.");
			}
				
			//return result;
		}
		
//		private AssignmentListDTO.AssignmentDTO createAssignmentDTO(Assignment a) {
//			Course c = a.getCourse();
//			AssignmentListDTO.AssignmentDTO assignmentDTO = new AssignmentListDTO.AssignmentDTO(a.getId(), c.getCourse_id(), a.getName(), a.getDueDate().toString(), c.getTitle());
//			return assignmentDTO;
//		}
		
		@PutMapping("/assignment/updateName")
		@Transactional
		public void updateAssignmentName (@RequestBody AssignmentListDTO.AssignmentDTO assignment) {
			
			Assignment a = assignmentRepository.findById(assignment.assignmentId).orElse(null);
			a.setName(assignment.assignmentName);
			
			System.out.printf("%s\n", a.toString());

			assignmentRepository.save(a);
		}
}
