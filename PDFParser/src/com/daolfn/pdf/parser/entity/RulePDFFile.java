package com.daolfn.pdf.parser.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Entity
public class RulePDFFile {
	private final static String TABLE_NAME = "rule_pdf_file";
	@Id
	@SequenceGenerator(name = TABLE_NAME+"_seq_generator", sequenceName = TABLE_NAME+"_seq", allocationSize = 1)
	private Long id;
	
}
