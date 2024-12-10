import { SearchField, Table, TextInput } from "@upyog/digit-ui-react-components";
import React, { useState, useEffect } from "react";

const CustomTable = ({
  columns,
  data,
  handleSort,
  currentPage,
  pageSizeLimit,
  sortParams,
  manualPagination,
  totalRecords,
  searchPlaceholder = "Search...",
  onSearchChange,
  filters = [],
  applyFilters,
  onNextPage,
  onPrevPage,
  onPageSizeChange,
  isPaginationRequired,
  onAddEmployeeClick,
  config,
  tableClassName = "",
  ...rest
}) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [filteredData, setFilteredData] = useState(data);

  useEffect(() => {
    if (searchTerm) {
      const searchResults = data.filter((row) =>
        Object.values(row).some((value) => value?.toString().toLowerCase().includes(searchTerm.toLowerCase()))
      );
      setFilteredData(searchResults);
    } else {
      setFilteredData(data);
    }
  }, [searchTerm, data]);

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    if (onSearchChange) onSearchChange(e.target.value);
  };

  // Render elements based on the passed config
  const renderElements = () => {
    if (!config || !config.elements || config.elements.length === 0) {
      return null; // Fallback message or UI
    }

    return config.elements.map((element, index) => {
      if (element.type === "p") {
        return (
          <p
            key={index}
            style={element.style}
            onClick={onAddEmployeeClick} // Dynamically map the onClick function
          >
            {element.text}
          </p>
        );
      }
      return null;
    });
  };

  return (
    <div>
      {/* Search Input */}
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <SearchField>
          <TextInput value={searchTerm} placeholder={"Search..."} onChange={handleSearchChange} style={{ width: "35%" }} />
        </SearchField>
        {renderElements()}
        <p>Total Records: {filteredData.length}</p>
      </div>

      {/* Render Table */}
      <Table
        className={`customTable table-fixed-first-column table-border-style deonar-scrollable-table ${tableClassName}`}
        data={filteredData}
        columns={columns}
        currentPage={currentPage}
        pageSizeLimit={pageSizeLimit}
        disableSort={false}
        autoSort={true}
        sortParams={sortParams}
        manualPagination={manualPagination}
        totalRecords={totalRecords}
        onNextPage={onNextPage}
        onPrevPage={onPrevPage}
        onPageSizeChange={onPageSizeChange}
        isPaginationRequired={isPaginationRequired}
        styles={{ border: "1px solid #ddd" }}
        initSortId="asc"
        getCellProps={(cellInfo) => ({
          style: {
            padding: "5px 5px",
            fontSize: "14px",
          },
        })}
        {...rest}
      />
    </div>
  );
};

export default CustomTable;
