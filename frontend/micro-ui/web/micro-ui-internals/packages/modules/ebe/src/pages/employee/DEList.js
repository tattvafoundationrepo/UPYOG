import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import CustomModal from "../../components/customModal";
import CustomTable from "../../components/customTable";
import { TextArea, FormComposer, EditIcon } from "@upyog/digit-ui-react-components";
import { newConfig } from "../../components/config/config";
import { PEConfig } from "../../components/config/PEConfig";
import WorkflowActions from "../../components/Workflow";
import { Link, useHistory} from "react-router-dom";
import StatusTags from '../../components/StatusTags';

const DEList = () => {
  const { t } = useTranslation();
  const [data1, setData] = useState([]);
  const [defaults, setDefaults] = useState({});
  const [selectedUUID, setSelectedUUID] = useState();
  const [totalRecords, setTotalRecords] = useState(0);
  const [sortParams, setSortParams] = useState({});
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [canSubmit, setSubmitValve] = useState(false);
  const tenantId = Digit.ULBService.getCurrentTenantId();
  //const appmutate = Digit.Hooks.ebe.useEBEProcessEnquiry();
  const appmutate = Digit.Hooks.ebe.useEBECreateEnquiry();
  const history = useHistory(); 
  const { isLoading, isError, error, data, ...restapply } = Digit.Hooks.ebe.useENQCount({action:null});
  let statusCounts ={};
  if(!isLoading){
    statusCounts = data.count;
  }
  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: defaults, mode: "onChange" });

  //const { submitForm, isSubmitting, response, error } = useSubmitForm(COLLECTION_POINT_ENDPOINT);
  const { data: test, isLoading: isEmployeeLoading } = Digit.Hooks.ebe.useGetEnquiry({type:1}, true);

  const dataArray = [];

  useEffect(() => {
    if (!isEmployeeLoading) {
      const test1 = test.peEnquiryList;
      dataArray.push(processdata(test1) || [])
      setData(dataArray[0]);
    }
  }, [isEmployeeLoading, test]);

  function processdata(test1) {
    return test1.map((ele) => {
      return ele.data1;
    });
  }

  const visibleColumns = [
    {
      Header: "PE Number",
      accessor: "enquiryCode",
      Cell: ({ row }) => (
        <span onClick={() => handleUUIDClick(row.original.enquiryCode)} style={{ cursor: "pointer", color: "blue" }}>
          {row.original.enquiryCode}
        </span>
      )
    },
    {
      Header: "Details",
      accessor: "orderDate",
      Cell: ({ row }) => (
        <div>
          <span>{t("Order_Number")}&nbsp; : &nbsp; </span><span>{row.original.orderNo}</span><span>&nbsp;&nbsp; {t("Dated")}&nbsp; : &nbsp; </span><span>{row.original.orderDate}</span><span>&nbsp;&nbsp; {t("Current State")}&nbsp; : &nbsp; </span><span>{row.original.currentState}</span>
          <TextArea name={"named" + row.index} value={row.original.enquirySubject} readOnly={true} />
        </div>
      )
    },
    {
      Header: "Actions", accessor: "Actions", Cell: ({ row }) => (<span><Link to={`/digit-ui/employee/ebe/PEUpdate/1/${row.original.enquiryCode}`}><EditIcon style={{ cursor: "pointer" }} /></Link></span>)
    }
  ]
    ;
  const _submit = () => {
    //submitAction(myData, action);
  };
  const onSubmit = async (formData) => {
    // try {
    //   const result = await submitForm(formData);
    //   console.log("Form successfully submitted:", result);
    //   alert("Form submission successful!");
    // } catch (error) {
    //   console.error("Error submitting form:", error);
    //   alert("Form submission failed");
    // }
  };

  const handleUUIDClick = (entryUnitId) => {
    setSelectedUUID(entryUnitId);
    sessionStorage.setItem("WorkingID", entryUnitId);
    setIsModalOpen(!isModalOpen);
  };

  const toggleModal = () => {
    if (isModalOpen) {
      // Clear the session storage when the modal closes
      // sessionStorage.removeItem("WorkingID");
    }
    setIsModalOpen(!isModalOpen);
  };

  const handleAddEmployee = () => {
    history.push("/digit-ui/employee/ebe/PECreate");
  };

  let myData = {};
  const onFormValueChange = (setValue = true, formData) => {
    myData = formData;
  };

  const defaultValues = {
    "enqId": selectedUUID
  };


  return (
    <React.Fragment>
      <div className="bmc-card-full">
      <div className="bmc-card-row"><div className="bmc-row-card-header"><StatusTags statusCounts={statusCounts} /></div></div>
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="bmc-card-row">
            <div className="bmc-row-card-header">
              <CustomTable
                t={t}
                columns={visibleColumns}
                data={data1}
                manualPagination={false}
                totalRecords={totalRecords}
                sortParams={sortParams}
                onAddEmployeeClick={handleAddEmployee}
              //  fileName="YourCustomFileName"
              />
              <CustomModal isOpen={isModalOpen} onClose={toggleModal} title={<h1 className="heading-m">{t("EBE_DE_TITLE")}</h1>} hideSubmit={true}>
                <FormComposer
                  formId={"modal-action1"}
                  defaultValues={defaultValues}
                  heading={t("")}
                  config={PEConfig("DE")}
                  onSubmit={handleSubmit(_submit)}
                  onFormValueChange={onFormValueChange}
                  isDisabled={!canSubmit}
                  label={t("HR_COMMON_BUTTON_SUBMIT")}
                />
                <WorkflowActions
                  ActionBarStyle={{}}
                  MenuStyle={{}}
                  businessService={"PE-Service"}
                  applicationNo={selectedUUID}
                  tenantId={tenantId}
                  moduleCode={"EBE"}
                  applicationDetails={{}}
                  customfunction={appmutate}
                  modalFormConfig={newConfig}
                />
              </CustomModal>
            </div>
          </div>
        </form>
      </div>
    </React.Fragment>
  );
};

export default DEList;
