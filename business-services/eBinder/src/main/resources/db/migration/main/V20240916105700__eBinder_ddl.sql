--Create table if not EXISTS eg_pe_enquiry
 CREATE TABLE eg_pe_enquiry(
 id SERIAL PRIMARY KEY,
 newCode VARCHAR(255) NOT NULL,
 oldCode VARCHAR(255) NOT NULL,
 orderDate BIGINT NOT NULL ,
 orderNo VARCHAR(255) NOT NULL,
 ceDesig VARCHAR(255) NOT NULL,
 ceDept VARCHAR(255) NOT NULL,
 ceEmpCode VARCHAR(255) NOT  NULL,
 enqSubject VARCHAR(500) NOT NULL,
 tenantId VARCHAR(255) NOT NULL,
 createdAt BIGINT NOT NULL,
 updatedAt BIGINT,
 createdBy VARCHAR(50) NOT NULL,
 updatedBy VARCHAR(50),
 CONSTRAINT fk_ceEmpCode FOREIGN KEY (ceEmpCode)
 REFERENCES public.eg_hrms_employee(uuid) MATCH SIMPLE
 ON UPDATE CASCADE
 ON DELETE CASCADE
 );

 --Create table if not EXISTS eg_ce_list
 CREATE TABLE eg_ce_list(
 id SERIAL PRIMARY KEY,
 peEnquiryId INT NOT NULL,
 ceCode VARCHAR(255) NOT NULL,
 ceName VARCHAR(255) ,
 ceDept VARCHAR NOT NULL,
 ceDesig VARCHAR(255) NOT NULL,
 ceSuspended BOOLEAN NOT NULL,
 ceSuspensionOrder VARCHAR(200),
 ceStatus BOOLEAN NOT NULL,
 enqOrderType VARCHAR(255) NOT NULL,
 caseType VARCHAR(200) NOT NULL,
 createdAt BIGINT NOT NULL,
 updatedAt BIGINT,
 createdBy VARCHAR(50) NOT NULL,
 updatedBy VARCHAR(50),
 CONSTRAINT fk_peEnquiryId FOREIGN KEY (peEnquiryId)
 REFERENCES eg_pe_enquiry (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE,
 CONSTRAINT fk_ceCode FOREIGN KEY (ceCode)
 REFERENCES eg_hrms_employee(uuid)
 ON UPDATE CASCADE
 ON DELETE CASCADE
 );


 --Create table if not EXISTS eg_pe_enq_records
 CREATE TABLE eg_pe_enq_records(
 id SERIAL PRIMARY KEY,
 peEnquiryId INT NOT NULL,
 specialComment 	VARCHAR(255),
 dates BIGINT NOT NULL,
 actions VARCHAR(255),
 createdAt BIGINT NOT NULL,
 updatedAt BIGINT,
 createdBy VARCHAR(50),
 updatedBy VARCHAR(50),
 CONSTRAINT fk_peEnquiryId FOREIGN KEY (peEnquiryId)
 REFERENCES eg_pe_enquiry (id)
 ON UPDATE CASCADE
 ON DELETE CASCADE
 );