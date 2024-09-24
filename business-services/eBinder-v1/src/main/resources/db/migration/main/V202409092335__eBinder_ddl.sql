--Create table if not EXISTS eg_eBinder_preliminary_enquiry
CREATE TABLE eg_eBinder_preliminary_enquiry(
id SERIAL PRIMARY KEY,
peNumber VARCHAR(255) UNIQUE,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(50),
updatedBy VARCHAR(50)
);

--Create table if not EXISTS eg_eBinder_pe_sanction_details
CREATE TABLE eg_eBinder_pe_sanction_details(
id SERIAL PRIMARY KEY,
subject VARCHAR(500) NOT NULL,
peCode VARCHAR(255) NOT NULL,
peOrderNumber VARCHAR(255) NOT NULL,
peOrderDate BIGINT NOT NULL,
peNumber int NOT NULL,
designationName VARCHAR(255) NOT NULL,
competentAuthName VARCHAR(255),
deptName VARCHAR(255) NOT NULL,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(50),
updatedBy VARCHAR(50),
CONSTRAINT fk_peNumber FOREIGN KEY (peNumber)
REFERENCES eg_eBinder_preliminary_enquiry (id));

--Create table if not EXISTS eg_eBinder_pe_report
CREATE TABLE eg_eBinder_pe_report(
id SERIAL PRIMARY KEY,
peNumber int NOT NULL,
reportNumber VARCHAR(200) NOT NULL,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(50),
updatedBy VARCHAR(50),
CONSTRAINT fk_peNumber FOREIGN KEY (peNumber)
REFERENCES eg_eBinder_preliminary_enquiry (id)
);

--Create table if not EXISTS eg_eBinder_pe_case_type
CREATE TABLE eg_eBinder_pe_case_type(
id SERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(50),
updatedBy VARCHAR(50)

);

--Create table if not EXISTS eg_eBinder_pe_Displinary_Order_type
CREATE TABLE eg_eBinder_pe_Displinary_Order_type(
id SERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(50),
updatedBy VARCHAR(50));

--Create table if not EXISTS eg_eBinder_pe_submission_report
CREATE TABLE eg_eBinder_pe_submission_report(
id SERIAL PRIMARY KEY,
peNumber int NOT NULL,
reportNumber int NOT NULL,
caseType int NOT NULL,
orderType int NOT NULL,
reportSubmissiomDate BIGINT NOT NULL,
designationName  VARCHAR(255) NOT NULL ,
competentAuthName  VARCHAR(255) NOT NULL,
createdAt BIGINT NOT NULL,
updatedAt BIGINT,
createdBy VARCHAR(50),
updatedBy VARCHAR(50),
CONSTRAINT fk_peNumber FOREIGN KEY (peNumber)
REFERENCES eg_eBinder_preliminary_enquiry (id),
CONSTRAINT fk_reportNumber FOREIGN KEY (reportNumber)
REFERENCES eg_eBinder_pe_report (id),
CONSTRAINT fk_caseType FOREIGN KEY (caseType)
REFERENCES eg_eBinder_pe_case_type (id),
CONSTRAINT fk_orderType FOREIGN KEY (orderType)
REFERENCES eg_eBinder_pe_Displinary_Order_type (id)
);

