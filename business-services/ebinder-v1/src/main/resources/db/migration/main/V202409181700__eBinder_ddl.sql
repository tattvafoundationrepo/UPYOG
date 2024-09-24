

CREATE TABLE IF NOT EXISTS eg_ebinder_pe_case_type (
	id serial NOT NULL,
	"name" varchar(255) NOT NULL,
	createdat int8 NOT NULL,
	updatedat int8 NULL,
	createdby varchar(50) NULL,
	updatedby varchar(50) NULL,
	CONSTRAINT eg_ebinder_pe_case_type_pkey PRIMARY KEY (id)
);




CREATE TABLE IF NOT EXISTS eg_ebinder_pe_displinary_order_type (
	id serial NOT NULL,
	"name" varchar(255) NOT NULL,
	createdat int8 NOT NULL,
	updatedat int8 NULL,
	createdby varchar(50) NULL,
	updatedby varchar(50) NULL,
	CONSTRAINT eg_ebinder_pe_displinary_order_type_pkey PRIMARY KEY (id)
);




CREATE TABLE IF NOT EXISTS eg_ebinder_pe_submission_report (
	id serial NOT NULL,
	penumber int NOT NULL,
	reportnumber int NOT NULL,
	reportsubmissiomdate int8 NOT NULL,
	designationname varchar(255) NOT NULL,
	competentauthname varchar(255) NOT NULL,
	createdat int8 NOT NULL,
	updatedat int8 NULL,
	createdby varchar(50) NULL,
	updatedby varchar(50) NULL,
	CONSTRAINT eg_ebinder_pe_submission_report_pkey PRIMARY KEY (id)

);




CREATE TABLE IF NOT EXISTS eg_ebinder_pe_enquiry (
	id serial NOT NULL,
	newcode varchar(255) NOT NULL,
	oldcode varchar(255) NOT NULL,
	orderdate int8 NOT NULL,
	orderno varchar(255) NOT NULL,
	cedesig varchar(255) NOT NULL,
	cedept varchar(255) NOT NULL,
	ceempcode varchar(255) NOT NULL,
	enqsubject varchar(500) NOT NULL,
	tenantid varchar(255) NOT NULL,
	createdat int8 NOT NULL,
	updatedat int8 NULL,
	createdby varchar(50) NOT NULL,
	updatedby varchar(50) NULL,
	CONSTRAINT eg_pe_enquiry_pkey PRIMARY KEY (id),
	CONSTRAINT uni_newcode UNIQUE (newcode)
);






CREATE TABLE IF NOT EXISTS eg_ebinder_ce_list (
	id serial NOT NULL,
	peenquiryid int NOT NULL,
	cecode varchar(255) NOT NULL,
	cename varchar(255) NOT NULL,
	cedept varchar(255) ,
	cedesig varchar(255) ,
	cesuspended bool DEFAULT false,
	cesuspensionorder varchar(200),
	cestatus bool DEFAULT true,
	enqordertype varchar(255),
	casetype varchar(200) ,
	createdat int NOT NULL,
	updatedat int ,
	createdby varchar(50) NOT NULL,
	updatedby varchar(50) ,
	mobileno varchar(15) ,
	email varchar(255) ,
	CONSTRAINT eg_ce_list_pkey PRIMARY KEY (id),
	CONSTRAINT uni_eg_ce_list UNIQUE (peenquiryid, cecode),
	CONSTRAINT fk_peenquiryid FOREIGN KEY (peenquiryid) REFERENCES eg_ebinder_pe_enquiry(id) ON DELETE CASCADE ON UPDATE CASCADE
);




CREATE TABLE IF NOT EXISTS eg_ebinder_pe_enq_records (
	id serial NOT NULL,
	peenquiryid int NOT NULL,
	comment varchar(255) ,
	dates BIGINT ,
	actions varchar NULL,
	createdat int8 NOT NULL,
	updatedat int8 ,
	createdby varchar(50) NOT NULL,
	updatedby varchar(50) ,
	CONSTRAINT pe_enq_records_pkey PRIMARY KEY (id),
	CONSTRAINT fk_peenquiryid FOREIGN KEY (peenquiryid) REFERENCES eg_ebinder_pe_enquiry(id)
);





