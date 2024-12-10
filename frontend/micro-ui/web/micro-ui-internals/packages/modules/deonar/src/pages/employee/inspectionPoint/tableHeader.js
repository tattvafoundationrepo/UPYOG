import React from "react";
import { useTranslation } from "react-i18next";
import { EditIcon } from "@upyog/digit-ui-react-components";

const InspectionTableHeader = ({ inspectionType, openModal }) => {
  const { t } = useTranslation();

  const getTableColumns = (inspectionType) => {
    switch (inspectionType) {
      case 1: //"Ante-Mortem_Inspection":
        return [
          // {
          //   Header: "Edit",
          //   accessor: "edit",
          //   Cell: ({ row }) => (
          //     <span onClick={() => openModal(row.original)}>
          //       <EditIcon style={{ cursor: "pointer" }} />
          //     </span>
          //   ),
          // },
          {
            Header: t("Deonar_Animal_Token_Number"),
            accessor: "animal",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Opinion"),
            accessor: "opinion",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Others"),
            accessor: "other",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Remark"),
            accessor: "resultremark",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Species"),
            accessor: "species",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Breed"),
            accessor: "breed",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Sex"),
            accessor: "sex",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Body_Color"),
            accessor: "bodyColor",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Eyes"),
            accessor: "eyes",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Pregnancy"),
            accessor: "pregnancy",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Gait"),
            accessor: "gait",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Posture"),
            accessor: "posture",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Body_Temp"),
            accessor: "bodyTemp",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Approx_Age"),
            accessor: "approxAge",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Pulse_Rate"),
            accessor: "pulseRate",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Appetite"),
            accessor: "appetite",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Nostrils"),
            accessor: "nostrils",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Muzzle"),
            accessor: "muzzle",
            disableSortBy: true,
          },
        ];
      case 2: //"Re-Ante_Mortem_Inspection":
        return [
          // {
          //   Header: "Edit",
          //   accessor: "edit",
          //   Cell: ({ row }) => (
          //     <span onClick={() => openModal(row.original)}>
          //       <EditIcon style={{ cursor: "pointer" }} />
          //     </span>
          //   ),
          // },
          {
            Header: t("Deonar_Animal_Token_Number"),
            accessor: "animal",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Opinion"),
            accessor: "opinion",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Others"),
            accessor: "other",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Remark"),
            accessor: "resultremark",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Species"),
            accessor: "species",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Breed"),
            accessor: "breed",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Sex"),
            accessor: "sex",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Body_Color"),
            accessor: "bodyColor",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Eyes"),
            accessor: "eyes",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Pregnancy"),
            accessor: "pregnancy",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Gait"),
            accessor: "gait",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Posture"),
            accessor: "posture",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Body_Temp"),
            accessor: "bodyTemp",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Approx_Age"),
            accessor: "approxAge",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Pulse_Rate"),
            accessor: "pulseRate",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Appetite"),
            accessor: "appetite",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Nostrils"),
            accessor: "nostrils",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Muzzle"),
            accessor: "muzzle",
            disableSortBy: true,
          },
        ];
      case 3: //"Before-Slaughter_Inspection":
        return [
          // {
          //   Header: "Edit",
          //   accessor: "edit",
          //   Cell: ({ row }) => (
          //     <span onClick={() => openModal(row.original)}>
          //       <EditIcon style={{ cursor: "pointer" }} />
          //     </span>
          //   ),
          // },
          {
            Header: t("Deonar_Slaughter_Receipt_Number"),
            accessor: "slaughterReceiptNumber",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Animal_Token_Number"),
            accessor: "animal",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Opinion"),
            accessor: "opinion",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Others"),
            accessor: "other",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Remark"),
            accessor: "resultremark",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Species"),
            accessor: "species",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Breed"),
            accessor: "breed",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Sex"),
            accessor: "sex",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Body_Color"),
            accessor: "bodyColor",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Eyes"),
            accessor: "eyes",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Pregnancy"),
            accessor: "pregnancy",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Gait"),
            accessor: "gait",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Posture"),
            accessor: "posture",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Body_Temp"),
            accessor: "bodyTemp",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Approx_Age"),
            accessor: "approxAge",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Pulse_Rate"),
            accessor: "pulseRate",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Appetite"),
            accessor: "appetite",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Nostrils"),
            accessor: "nostrils",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Muzzle"),
            accessor: "muzzle",
            disableSortBy: true,
          },
        ];
      case 4: //"Post-Mortem_Inspection":
        return [
          // {
          //   Header: "Edit",
          //   accessor: "edit",
          //   Cell: ({ row }) => (
          //     <span onClick={() => openModal(row.original)}>
          //       <EditIcon style={{ cursor: "pointer" }} />
          //     </span>
          //   ),
          // },
          {
            Header: t("Deonar_Slaughter_Receipt_Number"),
            accessor: "slaughterReceiptNumber",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Animal_Token_Number"),
            accessor: "animal",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Opinion"),
            accessor: "opinion",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Others"),
            accessor: "other",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Remark"),
            accessor: "resultremark",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Species"),
            accessor: "species",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Breed"),
            accessor: "breed",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Sex"),
            accessor: "sex",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Body_Color"),
            accessor: "bodyColor",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Pregnancy"),
            accessor: "pregnancy",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_VISIBLE_MUCUS_MEMBRANE"),
            accessor: "visibleMucusMembrane",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_THORACIC_CAVITY"),
            accessor: "thoracicCavity",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Approx_Age"),
            accessor: "approxAge",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_ABDOMINAL_CAVITY"),
            accessor: "abdominalCavity",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_PELVIC_CAVITY"),
            accessor: "pelvicCavity",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Specimen_Collection"),
            accessor: "specimenCollection",
            disableSortBy: true,
          },
          {
            Header: t("Deonar_Special_Observation"),
            accessor: "specialObservation",
            disableSortBy: true,
          },
        ];
      default:
        return [];
    }
  };

  return getTableColumns(inspectionType);
};

export default InspectionTableHeader;
