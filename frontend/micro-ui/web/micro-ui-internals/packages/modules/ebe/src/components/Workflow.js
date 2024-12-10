import { ActionBar, Loader, Menu, SubmitBar, Toast } from "@upyog/digit-ui-react-components";
import React, { useCallback, useMemo, useRef, useState ,useEffect} from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import WorkflowPopup from "./Workflowpopup";

const WorkflowActions = ({
    businessService,
    tenantId,
    applicationNo: propApplicationNo,
    forcedActionPrefix,
    ActionBarStyle = {},
    MenuStyle = {},
    applicationDetails,
    url,
    setStateChanged,
    moduleCode,
    editApplicationNumber,
    editCallback,
    callback,
    customfunction,
    modalFormConfig
}) => {
    const history = useHistory();
    const { estimateNumber } = Digit.Hooks.useQueryParams();
    const { t } = useTranslation();
    const applicationNo = useMemo(() => propApplicationNo || "" || estimateNumber, [propApplicationNo, estimateNumber]);
    const appmutate = customfunction;
    const workflowmutate = Digit.Hooks.workflow.useWorkFlowTransit();
    //const appmutate = Digit.Hooks.ebe.useEBECreateEnquiry();
    const [displayMenu, setDisplayMenu] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [selectedAction, setSelectedAction] = useState(null);
    const [isEnableLoader, setIsEnableLoader] = useState(false);
    const [showToast, setShowToast] = useState(null);
    const menuRef = useRef();
    const user = useMemo(() => Digit.UserService.getUser(), []);
    const userRoles = useMemo(() => user?.info?.roles?.map((e) => e.code), [user]);

    const workflowDetails = Digit.Hooks.useWorkflowDetailsnew({
        tenantId,
        id: applicationNo,
        moduleCode: businessService,
        config: { enabled: true, cacheTime: 0 },
    });

    const actions = useMemo(() => (
        workflowDetails?.data?.actionState?.nextActions?.filter((e) => (
            userRoles.some((role) => e.roles?.includes(role)) || !e.roles
        )) || workflowDetails?.data?.nextActions?.filter((e) => (
            userRoles.some((role) => e.roles?.includes(role)) || !e.roles
        ))
    ), [workflowDetails, userRoles]);

    const closeMenu = useCallback(() => setDisplayMenu(false), []);

    const closeToast = useCallback(() => {
        setTimeout(() => setShowToast(null), 5000);
    }, []);

    Digit.Hooks.useClickOutside(menuRef, closeMenu, displayMenu);

    const closeModal = useCallback(() => {
        setSelectedAction(null);
        setShowModal(false);
        setShowToast({ warning: true, label: `WF_ACTION_CANCELLED` });
        closeToast();
    }, [closeToast]);

    const onActionSelect = useCallback((action) => {
        setDisplayMenu(false);
        setSelectedAction(action);
        setShowModal(true);
    }, []);
    
    const submitAction = useCallback((data, selectedAction) => {
        setShowModal(false);
        setIsEnableLoader(true);
        const application = {"action": selectedAction.action,"businessId": applicationNo};
        const appmutateObj = { ...applicationDetails,data,application };
        const mydata = {
            "ProcessInstances": [{
                "action": selectedAction.action,
                "comment": data.ActionComment.value + " : " + (data.ActionComment.comments || "N/A"),
                "businessId": applicationNo,
                "tenantId": tenantId,
                "businessService": businessService,
                "moduleName": moduleCode
            }]
        };
        // Perform the first mutation
        appmutate.mutate(appmutateObj, {
            onSuccess: (data, variables) => {
                sessionStorage.removeItem("GeneratedENQID");
                const workflowmutateObj = { ...mydata, selectedAction };
                // Perform the second mutation only after the first one is successful
                workflowmutate.mutate(workflowmutateObj, {
                    onSuccess: (data, variables) => {
                        setIsEnableLoader(false);
                        setShowToast({ 
                            label: Digit.Utils.locale.getTransformedLocale(`WF_UPDATE_SUCCESS_${businessService}_${selectedAction.action}`) 
                        });
                        callback?.onSuccess?.();
                        workflowDetails.revalidate();
                    },
                    onError: (error, variables) => {
                        setIsEnableLoader(false);
                        setShowToast({ 
                            error: true, 
                            label: Digit.Utils.locale.getTransformedLocale(`WF_UPDATE_ERROR_${businessService}_${selectedAction.action}`), 
                            isDleteBtn: true 
                        });
                        callback?.onError?.();
                    }
                });
            },
            onError: (error, variables) => {
                sessionStorage.removeItem("GeneratedENQID");
                setIsEnableLoader(false);
                setShowToast({ 
                    error: true, 
                    label: Digit.Utils.locale.getTransformedLocale(`WF_UPDATE_ERROR_${businessService}_${selectedAction.action}`), 
                    isDleteBtn: true 
                });
                callback?.onError?.();
            }
        });
    }, [businessService, callback, workflowmutate, appmutate, workflowDetails, applicationNo, moduleCode, tenantId, applicationDetails]);
    if (isEnableLoader) return <Loader />;

    return (
        <React.Fragment>
            {!workflowDetails?.isLoading && actions?.length > 0 && (
                <ActionBar style={{ ...ActionBarStyle }}>
                    {displayMenu ? (
                        <Menu
                            localeKeyPrefix={forcedActionPrefix || Digit.Utils.locale.getTransformedLocale(`WF_${businessService?.toUpperCase()}_ACTION`)}
                            options={actions}
                            optionKey={"action"}
                            t={t}
                            onSelect={onActionSelect}
                            style={MenuStyle}
                        />
                    ) : null}
                    <SubmitBar ref={menuRef} label={t("WORKS_ACTIONS")} onSubmit={() => setDisplayMenu(!displayMenu)} />
                </ActionBar>
            )}
            {showModal && (

                <WorkflowPopup
                    action={selectedAction}
                    tenantId={tenantId}
                    t={t}
                    closeModal={closeModal}
                    submitAction={submitAction}
                    businessService={businessService}
                    moduleCode={moduleCode}
                    applicationDetails={applicationDetails}
                    modalFormConfig={modalFormConfig}
                />
            )}
            {showToast && (
                <Toast
                    error={showToast?.error}
                    warning={t(showToast?.warning)}
                    label={t(showToast?.label)}
                    onClose={() => setShowToast(null)}
                    isDleteBtn={showToast?.isDleteBtn}
                />
            )}
        </React.Fragment>
    );
};

export default WorkflowActions;
