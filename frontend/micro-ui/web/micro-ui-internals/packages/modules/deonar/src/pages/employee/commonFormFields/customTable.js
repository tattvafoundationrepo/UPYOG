import React, { useState, useEffect, useMemo } from "react";
import { SearchField, Table, TextInput, Loader } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import ImportPermissionDateField from "./importPermissionDate";
import { useForm } from "react-hook-form";

const CustomTable = ({
  columns = [],
  data = [],
  currentPage,
  pageSizeLimit,
  manualPagination,
  totalRecords,
  searchPlaceholder = "Search...",
  onSearchChange,
  filters = [],
  applyFilters,
  onNextPage,
  onPrevPage,
  onPageSizeChange,
  isPaginationRequired = true,
  onAddEmployeeClick,
  config,
  tableClassName = "",
  sortBy = [],
  onSort,
  isLoadingRows = false,
  showSearch = true,
  showTotalRecords = true,
  showPagination = true,
  showPdfDownload = false,
  showExcelDownload = false,
  downloadFileName = "table-data",
  downloadableColumns,
  getDownloadData,
  pdfButtonText = "PDF",
  excelButtonText = "Excel",
  customDownloadButtonStyles = {},
  showAddButton = false,
  buttonText = "Add Employee",
  onAddClick,
  showDateColumn = false, // New prop to control date column visibility
  onDateChange, 
  ...rest
}) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [filteredData, setFilteredData] = useState(data);
  const [sortConfig, setSortConfig] = useState(
    sortBy.map((sort) => {
      const [key, direction] = sort.split(":");
      return { key, direction: direction || "asc" };
    })
  );
  const [isDownloading, setIsDownloading] = useState(false);
  const [isExportMenuOpen, setIsExportMenuOpen] = useState(false); // State for dropdown
  const { t } = useTranslation();
  const { control } = useForm();

  useEffect(() => {
    setFilteredData(data);
  }, [data]);

  const handleSort = (key) => {
    const newSortConfig = sortConfig.map((config) => {
      if (config.key === key) {
        return { ...config, direction: config.direction === "asc" ? "desc" : "asc" };
      }
      return config;
    });

    if (!newSortConfig.some((config) => config.key === key)) {
      newSortConfig.push({ key, direction: "asc" });
    }

    setSortConfig(newSortConfig);
    if (onSort) {
      onSort(newSortConfig);
    }
  };

  const sortedData = useMemo(() => {
    let sortableItems = [...filteredData];
    sortConfig.forEach(({ key, direction }) => {
      sortableItems.sort((a, b) => {
        if (a[key] < b[key]) {
          return direction === "asc" ? -1 : 1;
        }
        if (a[key] > b[key]) {
          return direction === "asc" ? 1 : -1;
        }
        return 0;
      });
    });
    return sortableItems;
  }, [filteredData, sortConfig]);

  useEffect(() => {
    let result = data;
    if (searchTerm) {
      result = data.filter((row) => Object.values(row).some((value) => value?.toString().toLowerCase().includes(searchTerm.toLowerCase())));
    }
    setFilteredData(result);
  }, [data, searchTerm]);

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    if (onSearchChange) onSearchChange(e.target.value);
  };

  const renderElements = () => {
    if (!config || !config.elements || config.elements.length === 0) {
      return null;
    }

    return config.elements.map((element, index) => {
      if (element.type === "p") {
        return (
          <p key={index} style={element.style} onClick={onAddEmployeeClick}>
            {element.text}
          </p>
        );
      }
      return null;
    });
  };

  const generateCSV = (data) => {
    if (!data.length) return "";

    const headers = Object.keys(data[0]);
    const csvRows = [
      headers.join(","),
      ...data.map((row) =>
        headers
          .map((header) => {
            const cell = row[header]?.toString() || "";
            return cell.includes(",") || cell.includes('"') ? `"${cell.replace(/"/g, '""')}"` : cell;
          })
          .join(",")
      ),
    ];
    return csvRows.join("\n");
  };

  // New PDF generation using HTML
  const generatePDF = (data) => {
    if (!data.length) return;

    const headers = Object.keys(data[0]);
    const printWindow = window.open("", "", "height=600,width=800");

    const htmlContent = `
        <!DOCTYPE html>
        <html>
          <head>
            <title>${downloadFileName}</title>
            <style>
              body {
                font-family: Arial, sans-serif;
                margin: 20px;
              }
              table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 20px;
              }
              th, td {
                border: 1px solid #ddd;
                padding: 12px 8px;
                text-align: left;
              }
              th {
                background-color: #f4f4f4;
                font-weight: bold;
              }
              tr:nth-child(even) {
                background-color: #f9f9f9;
              }
              .header {
                font-size: 24px;
                margin-bottom: 20px;
              }
              .footer {
                margin-top: 20px;
                font-size: 12px;
                color: #666;
              }
              @media print {
                body { margin: 0; padding: 20px; }
                .no-print { display: none; }
              }
            </style>
          </head>
          <body>
            <div class="header">${downloadFileName}</div>
            <table>
              <thead>
                <tr>${headers.map((header) => `<th>${header}</th>`).join("")}</tr>
              </thead>
              <tbody>
                ${data
                  .map(
                    (row) => `
                  <tr>
                    ${headers.map((header) => `<td>${row[header] || ""}</td>`).join("")}
                  </tr>
                `
                  )
                  .join("")}
              </tbody>
            </table>
            <div class="footer">Generated on ${new Date().toLocaleString()}</div>
            <div class="no-print">
              <button onclick="window.print();window.close()" 
                style="padding: 10px 20px; background-color: #4a90e2; color: white; 
                border: none; border-radius: 4px; cursor: pointer; margin-top: 20px;">
                Download PDF
              </button>
            </div>
          </body>
        </html>
      `;

    printWindow.document.write(htmlContent);
    printWindow.document.close();
  };

  const handleExcelDownload = async (e) => {
    e.stopPropagation();
    if (isDownloading) return;
    try {
      setIsDownloading(true);
      const downloadData = sortedData;
      if (!downloadData.length) {
        console.warn("No data available for download");
        return;
      }
      const csv = generateCSV(downloadData);
      const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `${downloadFileName}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error("Error downloading Excel:", error);
    } finally {
      setIsDownloading(false);
    }
  };

  const handlePDFDownload = (e) => {
    e.stopPropagation();
    if (isDownloading) return;
    try {
      setIsDownloading(true);
      const downloadData = sortedData;
      if (!downloadData.length) {
        console.warn("No data available for download");
        return;
      }
      generatePDF(downloadData);
    } catch (error) {
      console.error("Error downloading PDF:", error);
    } finally {
      setIsDownloading(false);
    }
  };

  const enhancedColumns = useMemo(() => {
    if (!showDateColumn) return columns;

    return [
      ...columns,
      {
        Header: t("DEONAR_IMPORT_PERMISSION_DATE"),
        accessor: "importPermissionDate",
        Cell: ({ row }) => (
          <ImportPermissionDateField
          showLabel= {false}
            control={control}
            data={row.original}
            setData={(newData) => {
              if (onDateChange) {
                onDateChange(row.index, newData);
              }
            }}
            style={{ margin: 0, padding: 0 }}
          />
        ),
      },
    ];
  }, [columns, showDateColumn, t, control, onDateChange]);


  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "1rem" }}>
        <div style={{ display: "flex", gap: "1rem", alignItems: "center", flex: "1" }}>
          {showSearch && (
            <SearchField>
              <TextInput value={searchTerm} placeholder={searchPlaceholder} onChange={handleSearchChange} style={{ width: "35%" }} />
            </SearchField>
          )}
        </div>

        <div style={{ display: "flex", gap: "1rem", alignItems: "center" }}>
          {(showPdfDownload || showExcelDownload) && (
            <div style={{ position: "relative" }}>
              <button
                style={{
                  padding: "8px 16px",
                  backgroundColor: "#038403",
                  color: "white",
                  borderRadius: "4px",
                  border: "none",
                  cursor: "pointer",
                  fontWeight: "bold",
                }}
                onClick={() => setIsExportMenuOpen((prev) => !prev)}
              >
                Export
              </button>
              {isExportMenuOpen && (
                <div
                  style={{
                    position: "absolute",
                    top: "100%",
                    left: 0,
                    backgroundColor: "white",
                    boxShadow: "0 2px 4px rgba(0, 0, 0, 0.2)",
                    zIndex: 10,
                    borderRadius: "4px",
                    overflow: "hidden",
                  }}
                >
                  {showExcelDownload && (
                    <div
                      style={{
                        padding: "8px 16px",
                        cursor: "pointer",
                        backgroundColor: "#f9f9f9",
                        borderBottom: "1px solid #ddd",
                      }}
                      onClick={handleExcelDownload}
                    >
                      Download as CSV
                    </div>
                  )}
                  {showPdfDownload && (
                    <div
                      style={{
                        padding: "8px 16px",
                        cursor: "pointer",
                        backgroundColor: "#f9f9f9",
                      }}
                      onClick={handlePDFDownload}
                    >
                      Download as PDF
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
          {renderElements()}

          {showTotalRecords && (
            <p>
              {t("Total Records")}: {sortedData.length}
            </p>
          )}
        </div>
      </div>

      <Table
        className={`customTable table-fixed-first-column table-border-style deonar-scrollable-table ${tableClassName}`}
        columns={enhancedColumns.map((col) => ({
          ...col,
          sortFn: col.sortable ? () => handleSort(col.accessor) : undefined,
          isSortable: col.sortable,
          isSorted: col.sortable && sortConfig.some((config) => config.key === col.accessor),
          sortOrder: col.sortable ? sortConfig.find((config) => config.key === col.accessor)?.direction : undefined,
        }))}
        currentPage={currentPage}
        data={isLoadingRows ? [] : sortedData}
        pageSizeLimit={pageSizeLimit}
        disableSort={false}
        manualPagination={manualPagination}
        totalRecords={totalRecords}
        onNextPage={onNextPage}
        onPrevPage={onPrevPage}
        onPageSizeChange={onPageSizeChange}
        isPaginationRequired={isPaginationRequired && showPagination}
        styles={{ border: "1px solid #ddd" }}
        getCellProps={(cellInfo) => ({
          style: {
            padding: "5px 5px",
            fontSize: "14px",
          },
        })}
        {...rest}
      />

      {showAddButton && (
        <div style={{ paddingBottom: "1rem" }}>
          <button onClick={onAddClick} className="bmc-card-button" style={{ borderBottom: "3px solid black", outline: "none" }} type="submit">
            {buttonText}
          </button>
        </div>
      )}

      {isLoadingRows && (
        <div className="loader-overlay" style={{ position: "relative" }}>
          <Loader style={{ position: "absolute", top: "50%", left: "50%", transform: "translate(-50%, -50%)" }} />
        </div>
      )}
    </div>
  );
};

export default CustomTable;
