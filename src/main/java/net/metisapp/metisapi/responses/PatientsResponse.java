package net.metisapp.metisapi.responses;

import net.metisapp.metisapi.entities.MetisUser;

import java.util.List;

public class PatientsResponse implements StandardResponse{
		public final long patient_num;
		public final List<MetisUser> patients;
		public PatientsResponse(long num, List<MetisUser> patients){
			this.patient_num = num;
			this.patients = patients;
		}
}
