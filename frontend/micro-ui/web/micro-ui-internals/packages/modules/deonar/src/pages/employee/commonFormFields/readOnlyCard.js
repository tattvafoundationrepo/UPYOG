import { AddIcon, Dropdown, Loader, RemoveIcon, TextInput } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";
import { fetchDeonarEntryFeeDetailsSuccess } from "../../../redux/actions/securityDataActions";

const ReadOnlyCard = ({ 
  initialRows = [], 
  AllowEdit = true, 
  AllowRemove = true, 
  AddOption = true, 
  onUpdate, 
  addRow, 
  removeRow, 
  onUUIDClick, 
  deonarLabel, 
  columns = [], 
  visibleColumns = [],
  data = [] 
}) => {
  const [isEditable, setIsEditable] = useState(AllowEdit);
  const [rows, setRows] = useState([]);
  const { t } = useTranslation();
  // const arrivalUUID = "ARR1001"; 

  const { fetchEntryFeeDetailsbyUUID } = useDeonarCommon();
  const { data: fetchedData, error, isLoading } = fetchEntryFeeDetailsbyUUID({  });

  const dispatch = useDispatch();

  useEffect(() => {
    if (fetchedData) {
      setRows(fetchedData.SecurityCheckDetails);
      dispatch(fetchDeonarEntryFeeDetailsSuccess(fetchedData));
    }
  }, [fetchedData, dispatch]);

  const displayedColumns = columns.filter((col) =>
    visibleColumns.includes(col.key)
  );

  const handleUUIDClick = (uuid) => {
    if (onUUIDClick) {
      onUUIDClick(uuid);
    }
  };

  if (isLoading) {
    return <Loader />;
  }

  if (error) return <div>{t("ERROR_LOADING_DATA")}</div>;

  return (
    <div>
      <div className="bmc-title" deonarLabel={deonarLabel}></div>
      <div className="bmc-table-container" style={{ padding: "1rem" }}>
        <table className="bmc-hover-table">
          <thead>
            <tr>
              {displayedColumns.map((col, index) => (
                <th key={index} scope="col">
                  {t(col.header)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows?.map((row, index) => (
              <tr key={index}>
                {displayedColumns.map((col) => (
                  <td key={col.key}>
                    {col.render
                      ? col.render(row[col.key], row, handleUUIDClick, index) 
                      : row[col.key]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ReadOnlyCard;
