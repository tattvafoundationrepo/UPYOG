import Title from "../../components/title";
import React, { useCallback, useState,useEffect} from "react";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import EmployeeDetails from "../../components/EmployeeDetails";
import PreEnquiryCard from "../../components/PreEnquiry";
import WorkflowActions from "../../components/Workflow";
import WorkflowTimeline from "../../components/WorkflowTimeline";
import { newConfig } from "../../components/config/config";

const PreEnquiryMain = () => {
    const location = useLocation();
    const { t } = useTranslation();
    //const { Enquiry, EnquiryNumber } = location.state || {};
    const tenantId = Digit.ULBService.getCurrentTenantId();
    const [data1,setData1]=useState([]);
    const [data2,setData2]=useState([]);
    const appmutate = Digit.Hooks.ebe.useEBECreateEnquiry();
    const GeneratedENQID = sessionStorage.getItem("GeneratedENQID")
    const shouldCallHook = useState(!GeneratedENQID);
    //const { data, isLoading } = Digit.Hooks.hrms.useHrmsMDMS(tenantId, "egov-hrms", "HRMSRolesandDesignation");
    const { data: EstimatedID, isLoading: loadingFlag } = Digit.Hooks.ebe.useEstimatedID({ "idRequests": [{ "tenantId": tenantId, "idName": "enq.enquiryid" }] }, shouldCallHook[0], {});
    useEffect(() => {
      if (!loadingFlag && EstimatedID) {
        sessionStorage.setItem("GeneratedENQID",EstimatedID?.idResponses[0].id);
      }
    }, [EstimatedID, loadingFlag]);
    //const data = Enquiry
    //const titleData = data.title.split("_");
    //data.title = titleData[titleData.length - 1];
    const handleCallback1 = useCallback((data) => {
        setData1(data);
    },[]);
    const handleCallback2 = useCallback((data) => {
        setData2(data);
    }, []);

    return (
        <React.Fragment>
            <div className="bmc-card-full">
                <Title text={t("Prelimanery Enquiry")} />
                <PreEnquiryCard onUpdate={handleCallback1} initialRows={{}} tenantId={tenantId} AllowEdit={true}></PreEnquiryCard>
                <EmployeeDetails onUpdate={handleCallback2} initialRows={{}} tenantId={tenantId} AddOption={true} AllowRemove={true} />
                <WorkflowTimeline tenantId={tenantId} businessService={"PE-Service"} applicationNo={"test-0003"}></WorkflowTimeline>
                <WorkflowActions
                    url={"/ebinder-v1/peEnquiry/_save"}
                    ActionBarStyle={{}}
                    MenuStyle={{}}
                    businessService={"PE-Service"}
                    applicationNo={GeneratedENQID}
                    tenantId={tenantId}
                    moduleCode={"EBE"}
                    applicationDetails={{data1,data2}}
                    customfunction = {appmutate}
                    modalFormConfig={newConfig}
                />
            </div>
        </React.Fragment>
    );
};

export default PreEnquiryMain;